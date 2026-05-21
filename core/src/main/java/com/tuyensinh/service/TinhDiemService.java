package com.tuyensinh.service;

import com.tuyensinh.dao.*;
import com.tuyensinh.entity.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Engine tinh diem xet tuyen.
 *
 * Huong xu ly chinh:
 * - THPT / VSAT: tinh theo tung mon trong to hop
 * - DGNL: lay diem tong NL1 va quy doi theo bang dai dien A01/B00/C01/D01 neu to hop NV khong co bang rieng
 * - Diem quy doi chung chi Anh: doc tu xt_thisinh_chungchi.ghi_chu (excel_diem_quy_doi)
 * - Diem cong chung chi / thanh tich: doc tu ghi_chu cua bang nguon import
 * - Diem cong tong = chung chi + uu tien xet tuyen + uu tien quy che
 */
public class TinhDiemService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal THIRTY = new BigDecimal("30");
    private static final int SCALE = 3;
    private static final RoundingMode ROUND = RoundingMode.HALF_UP;
    private static final short NAM_TUYEN_SINH_MAC_DINH = 2025;

    private final BangQuyDoiDao bangQuyDoiDao = new BangQuyDoiDao();
    private final DiemThiDao diemThiDao = new DiemThiDao();
    private final DiemCongDao diemCongDao = new DiemCongDao();
    private final ToHopDao toHopDao = new ToHopDao();
    private final PhuongThucDao phuongThucDao = new PhuongThucDao();
    private final NguyenVongDao nguyenVongDao = new NguyenVongDao();
    private final NganhToHopDao nganhToHopDao = new NganhToHopDao();
    private final MaXetTuyenMapDao maXetTuyenMapDao = new MaXetTuyenMapDao();

    // Cache nho de khi xet tuyen toan bo khong phai truy van lai map DGNL lap lai qua nhieu lan.
    private final Map<Integer, List<ToHop>> dgnlToHopTheoNganhCache = new HashMap<>();
    private final Map<String, ToHop> toHopDaiDienCache = new HashMap<>();

    private final DiemCongService diemCongService = new DiemCongService();
    private final DiemCongChiTietService diemCongChiTietService = new DiemCongChiTietService();
    private final ThiSinhChungChiService thiSinhChungChiService = new ThiSinhChungChiService();
    private final ThiSinhThanhTichService thiSinhThanhTichService = new ThiSinhThanhTichService();

    public static class KetQuaDiem {
        public BigDecimal diemThxt;
        public BigDecimal diemCong;
        public BigDecimal diemUutien;
        public BigDecimal diemXettuyen;
        public BigDecimal diemThpt;
        public BigDecimal diemVsat;
        public BigDecimal diemDgnl;
        public boolean coBangQuyDoi;
        public String phuongThucDiemTotNhat;
        public String toHopDiemTotNhat;
        public String ghiChu;

        @Override
        public String toString() {
            return String.format(
                    "Nguon=%s | THM=%s | THXT=%.2f | Cong=%.2f (UT=%.2f) | XetTuyen=%.2f | BQD=%s | THPT=%.2f | VSAT=%.2f | DGNL=%.2f | GhiChu=%s",
                    phuongThucDiemTotNhat != null ? phuongThucDiemTotNhat : "?",
                    toHopDiemTotNhat != null ? toHopDiemTotNhat : "?",
                    safeForLog(diemThxt),
                    safeForLog(diemCong),
                    safeForLog(diemUutien),
                    safeForLog(diemXettuyen),
                    coBangQuyDoi,
                    safeForLog(diemThpt),
                    safeForLog(diemVsat),
                    safeForLog(diemDgnl),
                    ghiChu
            );
        }

        private static BigDecimal safeForLog(BigDecimal value) {
            return value != null ? value : BigDecimal.ZERO;
        }
    }


    /** Ket qua tong hop khi tao diem cong toan bo bang che do batch. */
    public static class KetQuaTaoDiemCongBatch {
        public int processed;
        public int success;
        public int skipped;
        public int error;
        public int soDiemCong;
        public int soChiTiet;
        public boolean clearedOldData;
        public final List<String> logRutGon = new ArrayList<>();

        public void addLog(String message) {
            if (message == null || message.trim().isEmpty()) return;
            if (logRutGon.size() < 80) {
                logRutGon.add(message);
            }
        }

        public String getLogText() {
            if (logRutGon.isEmpty()) return "Khong co loi/ghi chu chi tiet.";
            return String.join("\n", logRutGon);
        }
    }

    /**
     * Tinh diem xet tuyen cho 1 nguyen vong.
     */
    public KetQuaDiem tinhDiem(NguyenVong nv, DiemThi diemThi, Optional<DiemCong> diemCongOpt) {
        KetQuaDiem kq = new KetQuaDiem();
        kq.diemThxt = ZERO;
        kq.diemCong = ZERO;
        kq.diemUutien = ZERO;
        kq.diemXettuyen = ZERO;
        kq.coBangQuyDoi = false;
        kq.ghiChu = null;

        if (nv == null) {
            kq.ghiChu = "Nguyen vong null";
            return kq;
        }

        NganhToHop nth = nv.getNganhToHop();
        if (nth == null) {
            kq.ghiChu = "Khong co thong tin nganh-to-hop";
            return kq;
        }

        ToHop toHop = nth.getToHop();
        if (toHop == null) {
            kq.ghiChu = "Khong co thong tin to hop";
            return kq;
        }
        kq.toHopDiemTotNhat = safe(toHop.getMaTohop());

        PhuongThuc pt = nv.getPhuongThuc();
        if (pt == null) {
            kq.ghiChu = "Khong co thong tin phuong thuc";
            return kq;
        }

        List<ToHopMon> monToHop = toHopDao.findMonByToHopId(toHop.getTohopId());
        if (monToHop == null || monToHop.isEmpty()) {
            kq.ghiChu = "To hop khong co mon";
            return kq;
        }

        Map<Integer, BigDecimal> heSoMap = new HashMap<>();
        if (nth.getDanhSachNganhToHopMon() != null) {
            for (NganhToHopMon nthm : nth.getDanhSachNganhToHopMon()) {
                if (nthm.getMon() != null && nthm.getMon().getMonId() != null) {
                    heSoMap.put(
                            nthm.getMon().getMonId(),
                            nthm.getHeSo() != null ? new BigDecimal(nthm.getHeSo()) : ONE
                    );
                }
            }
        }

        Map<Integer, DiemThiChiTiet> diemTheoMon = new HashMap<>();
        if (diemThi != null && diemThi.getDanhSachDiemChiTiet() != null) {
            for (DiemThiChiTiet ct : diemThi.getDanhSachDiemChiTiet()) {
                if (ct.getMon() != null && ct.getMon().getMonId() != null) {
                    diemTheoMon.put(ct.getMon().getMonId(), ct);
                }
            }
        }

        BigDecimal diemQuyDoiTiengAnhTuCc = layDiemQuyDoiTiengAnhTuDiemCong(diemCongOpt.orElse(null));
        List<String> notes = new ArrayList<>();

        // =========================
        // NHANH RIENG CHO DGNL
        // =========================
        if (isPhuongThucDGNLHCM(pt)) {
            BigDecimal diemDgnl = timDiemDgnlTuChiTiet(diemTheoMon);

            if (diemDgnl == null) {
                diemDgnl = ZERO;
                notes.add("Khong co diem DGNL (NL1)");
            } else {
                QuyDoiDgnlResult quyDoiDgnl = timQuyDoiDGNL(nv, pt, toHop, diemDgnl);
                if (quyDoiDgnl != null && quyDoiDgnl.diemQuyDoi != null) {
                    kq.diemThxt = chuanHoaDiemToHop(quyDoiDgnl.diemQuyDoi);
                    kq.coBangQuyDoi = true;

                    String maToHopGoc = toHop != null ? safe(toHop.getMaTohop()) : "";
                    String maToHopQuyDoi = quyDoiDgnl.toHopQuyDoi != null ? safe(quyDoiDgnl.toHopQuyDoi.getMaTohop()) : "";
                    String moTaToHop = maToHopQuyDoi;
                    if (!isBlank(maToHopGoc) && !maToHopGoc.equalsIgnoreCase(maToHopQuyDoi)) {
                        moTaToHop = maToHopQuyDoi + " (NV " + maToHopGoc + ")";
                    }

                    notes.add("DGNL " + diemDgnl.toPlainString() + " -> " + kq.diemThxt.toPlainString()
                            + " theo bang quy doi " + moTaToHop);
                } else {
                    kq.diemThxt = ZERO;
                    notes.add("Khong tim thay bang quy doi DGNL phu hop cho NV " + safe(toHop.getMaTohop()));
                }
            }

            /*
             * DGNL da duoc quy doi truc tiep ve diem to hop goc xet tuyen thang 30.
             * do_lech chi ap dung cho nhom diem tinh theo to hop mon nhu THPT/VSAT/NK.
             */

            if (diemCongOpt != null && diemCongOpt.isPresent()) {
                DiemCong dc = diemCongOpt.get();
                kq.diemCong = safe(dc.getTongDiemCong());

                BigDecimal utXt = safe(dc.getTongDiemUutienXt());
                BigDecimal utQc = safe(dc.getTongDiemUutienQuyChe());
                kq.diemUutien = utXt.add(utQc);
            }

            kq.diemXettuyen = capDiemXetTuyen(kq.diemThxt.add(kq.diemCong)).setScale(SCALE, ROUND);
            if (!notes.isEmpty()) {
                kq.ghiChu = String.join(" | ", notes);
            }
            return kq;
        }

        // =========================
        // NHANH CHUNG CHO THPT / VSAT / NK
        // =========================
        BigDecimal tongDiem = ZERO;
        BigDecimal tongHeSo = ZERO;

        for (ToHopMon thm : monToHop) {
            if (thm.getMon() == null || thm.getMon().getMonId() == null) continue;

            Mon mon = thm.getMon();
            Integer monId = mon.getMonId();
            BigDecimal heSo = heSoMap.getOrDefault(monId, ONE);
            if (heSo == null || heSo.compareTo(ZERO) <= 0) {
                heSo = ONE;
            }
            tongHeSo = tongHeSo.add(heSo);
            BigDecimal diemDung = null;

            // Neu mon la Anh va co quy doi tu chung chi -> uu tien dung diem quy doi nay
            if (laMonAnh(mon) && diemQuyDoiTiengAnhTuCc.compareTo(ZERO) > 0) {
                diemDung = diemQuyDoiTiengAnhTuCc;
                kq.coBangQuyDoi = true;
                notes.add("Dung diem quy doi chung chi cho mon " + safe(mon.getMaMon()));
            } else {
                DiemThiChiTiet ct = diemTheoMon.get(monId);
                if (ct != null) {
                    // Luon dung diem_goc lam dau vao bang quy doi.
                    // Truoc day code lay diem_sudung/diem_quydoi roi dem di quy doi tiep,
                    // nen VSAT/DGNL co nguy co bi quy doi sai hoac quy doi hai lan.
                    BigDecimal diemGocDeQuyDoi = ct.getDiemGoc();
                    if (diemGocDeQuyDoi == null) {
                        diemGocDeQuyDoi = ct.getDiemSudung();
                    }
                    if (diemGocDeQuyDoi == null) {
                        diemGocDeQuyDoi = ct.getDiemQuydoi();
                    }

                    if (diemGocDeQuyDoi != null) {
                        if (isPhuongThucVSAT(pt)) {
                            BigDecimal diemSauBangQd = timDiemQuyDoi(diemGocDeQuyDoi, pt, toHop, mon);
                            if (diemSauBangQd != null) {
                                diemDung = diemSauBangQd;
                                kq.coBangQuyDoi = true;
                            } else {
                                diemDung = ct.getDiemSudung();
                                if (diemDung == null) diemDung = ct.getDiemQuydoi();
                                if (diemDung == null) diemDung = ct.getDiemGoc();
                            }
                        } else {
                            // THPT/NK khong co bang quy doi trong xt_bangquydoi, dung truc tiep diem goc.
                            diemDung = diemGocDeQuyDoi;
                        }
                    }
                }
            }

            if (diemDung == null) {
                diemDung = ZERO;
                notes.add("Khong co diem mon " + safe(mon.getMaMon()));
            }

            tongDiem = tongDiem.add(diemDung.multiply(heSo).setScale(SCALE, ROUND));
        }

        if (tongHeSo.compareTo(ZERO) > 0) {
            // Theo tai lieu SGU: DTHXT = [(d1*w1 + d2*w2 + d3*w3) / W] * 3.
            // W = tong he so. Khong lay raw weighted sum vi se vuot thang 30.
            kq.diemThxt = chuanHoaDiemToHop(
                    tongDiem.divide(tongHeSo, SCALE, ROUND)
                            .multiply(new BigDecimal("3"))
            );
        } else {
            kq.diemThxt = ZERO;
        }

        BigDecimal doLech = nth.getDoLech();
        if (doLech != null && doLech.compareTo(ZERO) > 0) {
            kq.diemThxt = chuanHoaDiemToHop(kq.diemThxt.subtract(doLech));
            notes.add("Tru do lech " + doLech.toPlainString());
        }

        if (diemCongOpt != null && diemCongOpt.isPresent()) {
            DiemCong dc = diemCongOpt.get();
            kq.diemCong = safe(dc.getTongDiemCong());

            BigDecimal utXt = safe(dc.getTongDiemUutienXt());
            BigDecimal utQc = safe(dc.getTongDiemUutienQuyChe());
            kq.diemUutien = utXt.add(utQc);
        }

        kq.diemXettuyen = capDiemXetTuyen(kq.diemThxt.add(kq.diemCong)).setScale(SCALE, ROUND);
        if (!notes.isEmpty()) {
            kq.ghiChu = String.join(" | ", notes);
        }

        return kq;
    }

    /**
     * Quy doi diem bang BangQuyDoi.
     * Tra ve null neu khong co ban ghi quy doi phu hop.
     */
    public BigDecimal timDiemQuyDoi(BigDecimal diemGoc, PhuongThuc pt, ToHop toHop, Mon mon) {
        if (pt == null || diemGoc == null) return null;

        BangQuyDoi bqd = bangQuyDoiDao.quyDoiDiem(
                pt.getPhuongthucId(),
                toHop != null ? toHop.getTohopId() : null,
                mon != null ? mon.getMonId() : null,
                diemGoc
        );
        if (bqd == null) return null;

        BigDecimal tu = diemGoc.subtract(bqd.getDiemTu());
        BigDecimal den = bqd.getDiemDen().subtract(bqd.getDiemTu());
        BigDecimal qdDen = bqd.getDiemQuydoiDen().subtract(bqd.getDiemQuydoiTu());

        if (den.compareTo(ZERO) == 0) return null;

        BigDecimal diemQd = bqd.getDiemQuydoiTu()
                .add(tu.multiply(qdDen).divide(den, SCALE, ROUND));

        return diemQd.setScale(SCALE, ROUND);
    }

    /**
     * Tinh diem tot nhat cho mot nguyen vong.
     *
     * Nghiep vu ap dung cho du lieu hien tai:
     * - NguyenVong goc khong co cot phuong thuc rieng, da import chu yeu la THPT.
     * - Cung mot ma xet tuyen/nganh co the co cac dong map THPT, VSAT, DGNL trong xt_ma_xettuyen.
     * - Neu thi sinh co diem o nhieu nguon thi quy doi ve cung thang, cong diem cong/uu tien,
     *   sau do lay diem_xettuyen cao nhat.
     * - NK khong phai nguon diem canh tranh; NK duoc ghep vao THPT neu to hop co mon NK1..NK6.
     */
    public KetQuaDiem tinhDiemTotNhat(NguyenVong nv) {
        KetQuaDiem empty = new KetQuaDiem();
        empty.diemThxt = ZERO;
        empty.diemCong = ZERO;
        empty.diemUutien = ZERO;
        empty.diemXettuyen = ZERO;
        empty.diemThpt = ZERO;
        empty.diemVsat = ZERO;
        empty.diemDgnl = ZERO;
        empty.phuongThucDiemTotNhat = null;
        empty.toHopDiemTotNhat = null;
        empty.ghiChu = "Khong tinh duoc diem";

        if (nv == null || nv.getThiSinh() == null || nv.getNganhToHop() == null) {
            empty.ghiChu = "Nguyen vong thieu thi sinh/nganh-to-hop";
            return empty;
        }

        List<PhuongAnDiem> phuongAnList = taoDanhSachPhuongAnDiem(nv);
        if (phuongAnList.isEmpty()) {
            // Fallback: tinh theo phuong thuc dang luu tren nguyen vong.
            if (nv.getPhuongThuc() != null) {
                phuongAnList.add(new PhuongAnDiem(nv.getPhuongThuc(), nv.getNganhToHop()));
            } else {
                empty.ghiChu = "Khong co phuong thuc de tinh diem";
                return empty;
            }
        }

        KetQuaDiem best = null;
        List<String> tongHop = new ArrayList<>();

        for (PhuongAnDiem pa : phuongAnList) {
            KetQuaDiem kq = tinhDiemTheoPhuongAn(nv, pa.phuongThuc, pa.nganhToHop);
            String label = labelPhuongThuc(pa.phuongThuc);
            kq.phuongThucDiemTotNhat = label;
            if (isBlank(kq.toHopDiemTotNhat)) {
                kq.toHopDiemTotNhat = maToHopCuaPhuongAn(pa.nganhToHop);
            }

            BigDecimal diem = safe(kq.diemXettuyen);
            if (isPhuongThucTHPT(pa.phuongThuc)) {
                empty.diemThpt = empty.diemThpt.max(diem);
            } else if (isPhuongThucVSAT(pa.phuongThuc)) {
                empty.diemVsat = empty.diemVsat.max(diem);
            } else if (isPhuongThucDGNLHCM(pa.phuongThuc)) {
                empty.diemDgnl = empty.diemDgnl.max(diem);
            }

            String maToHop = pa.nganhToHop != null && pa.nganhToHop.getToHop() != null
                    ? safe(pa.nganhToHop.getToHop().getMaTohop())
                    : "?";
            tongHop.add(label + "(" + maToHop + ")=" + diem.setScale(2, ROUND).toPlainString());

            if (best == null || diem.compareTo(safe(best.diemXettuyen)) > 0) {
                best = kq;
            }
        }

        if (best == null) {
            return empty;
        }

        best.diemThpt = empty.diemThpt;
        best.diemVsat = empty.diemVsat;
        best.diemDgnl = empty.diemDgnl;

        String prefix = "Lay diem cao nhat: " + best.phuongThucDiemTotNhat
                + (isBlank(best.toHopDiemTotNhat) ? "" : "(" + best.toHopDiemTotNhat + ")")
                + " | " + String.join(" ; ", tongHop);
        best.ghiChu = prefix + (isBlank(best.ghiChu) ? "" : " | " + best.ghiChu);

        return best;
    }

    private String maToHopCuaPhuongAn(NganhToHop nth) {
        if (nth != null && nth.getToHop() != null && !isBlank(nth.getToHop().getMaTohop())) {
            return nth.getToHop().getMaTohop();
        }
        return null;
    }

    private static class PhuongAnDiem {
        final PhuongThuc phuongThuc;
        final NganhToHop nganhToHop;

        PhuongAnDiem(PhuongThuc phuongThuc, NganhToHop nganhToHop) {
            this.phuongThuc = phuongThuc;
            this.nganhToHop = nganhToHop;
        }
    }

    private static class QuyDoiDgnlResult {
        final ToHop toHopQuyDoi;
        final BigDecimal diemQuyDoi;

        QuyDoiDgnlResult(ToHop toHopQuyDoi, BigDecimal diemQuyDoi) {
            this.toHopQuyDoi = toHopQuyDoi;
            this.diemQuyDoi = diemQuyDoi;
        }
    }

    /**
     * DB hien tai khong luu bang quy doi DGNL cho moi to hop.
     * xt_bangquydoi chi co 4 bang dai dien: A01, B00, C01, D01.
     * Vi vay khi NV la A00/C04/C00/... can chon bang dai dien phu hop thay vi bat buoc tim dung tohop_id cua NV.
     */
    private QuyDoiDgnlResult timQuyDoiDGNL(NguyenVong nv, PhuongThuc pt, ToHop toHopHienTai, BigDecimal diemDgnl) {
        if (pt == null || diemDgnl == null) return null;

        // 1. Neu to hop hien tai co bang quy doi truc tiep thi dung luon.
        BigDecimal diemExact = timDiemQuyDoi(diemDgnl, pt, toHopHienTai, null); 
        if (diemExact != null) {
            return new QuyDoiDgnlResult(toHopHienTai, diemExact);
        }

        // 2. Neu khong co, chon bang dai dien theo nganh va theo do gan cua to hop.
        List<ToHop> ungVien = layDanhSachToHopQuyDoiDGNL(nv, toHopHienTai);
        Integer toHopHienTaiId = toHopHienTai != null ? toHopHienTai.getTohopId() : null;

        for (ToHop th : ungVien) {
            if (th == null || th.getTohopId() == null) continue;
            if (Objects.equals(toHopHienTaiId, th.getTohopId())) continue;

            BigDecimal diem = timDiemQuyDoi(diemDgnl, pt, th, null);
            if (diem != null) {
                return new QuyDoiDgnlResult(th, diem);
            }
        }

        return null;
    }

    private List<ToHop> layDanhSachToHopQuyDoiDGNL(NguyenVong nv, ToHop toHopHienTai) {
        LinkedHashMap<Integer, ToHop> map = new LinkedHashMap<>();

        Integer nganhId = layNganhId(nv, null);
        if (nganhId != null) {
            for (ToHop th : layToHopDgnlTheoNganh(nganhId)) {
                if (th != null && th.getTohopId() != null) {
                    map.putIfAbsent(th.getTohopId(), th);
                }
            }
        }

        // Fallback: suy luan bang dai dien theo ma/mon cua to hop hien tai.
        for (String ma : goiYMaToHopDgnl(toHopHienTai)) {
            ToHop th = findToHopDaiDien(ma);
            if (th != null && th.getTohopId() != null) {
                map.putIfAbsent(th.getTohopId(), th);
            }
        }

        List<ToHop> result = new ArrayList<>(map.values());
        List<String> thuTuUuTien = goiYMaToHopDgnl(toHopHienTai);
        result.sort(Comparator.comparingInt(th -> viTriUuTienToHop(th, thuTuUuTien)));
        return result;
    }

    private List<ToHop> layToHopDgnlTheoNganh(Integer nganhId) {
        if (nganhId == null) return Collections.emptyList();

        List<ToHop> cached = dgnlToHopTheoNganhCache.get(nganhId);
        if (cached != null) {
            return cached;
        }

        LinkedHashMap<Integer, ToHop> map = new LinkedHashMap<>();
        try {
            List<MaXetTuyenMap> maps = maXetTuyenMapDao.findByNganhIdWithDetails(nganhId);
            for (MaXetTuyenMap m : maps) {
                if (m == null || Boolean.FALSE.equals(m.getIsActive())) continue;
                if (m.getNganhToHop() == null || m.getNganhToHop().getToHop() == null) continue;

                ToHop th = m.getNganhToHop().getToHop();
                if (isMaToHopDgnlDaiDien(th.getMaTohop())) {
                    map.putIfAbsent(th.getTohopId(), th);
                }
            }
        } catch (Exception ex) {
            return Collections.emptyList();
        }

        List<ToHop> result = new ArrayList<>(map.values());
        dgnlToHopTheoNganhCache.put(nganhId, result);
        return result;
    }

    private List<String> goiYMaToHopDgnl(ToHop toHopHienTai) {
        List<String> result = new ArrayList<>();
        String ma = toHopHienTai != null ? safe(toHopHienTai.getMaTohop()).trim().toUpperCase(Locale.ROOT) : "";

        if (isMaToHopDgnlDaiDien(ma)) themNeuChuaCo(result, ma);

        if (ma.startsWith("A")) {
            themNeuChuaCo(result, "A01");
        } else if (ma.startsWith("B")) {
            themNeuChuaCo(result, "B00");
        } else if (ma.startsWith("C")) {
            themNeuChuaCo(result, "C01");
            themNeuChuaCo(result, "D01");
        } else if (ma.startsWith("D")) {
            themNeuChuaCo(result, "D01");
        }

        // Cac ma X/Y moi duoc sinh tu tohopmon.xlsx, suy luan theo mon thanh phan.
        if (toHopCoMon(toHopHienTai, "N1")) themNeuChuaCo(result, "D01");
        if (toHopCoMon(toHopHienTai, "SI")) themNeuChuaCo(result, "B00");
        if (toHopCoMon(toHopHienTai, "LI") || toHopCoMon(toHopHienTai, "HO")) themNeuChuaCo(result, "A01");
        if (toHopCoMon(toHopHienTai, "VA")) {
            themNeuChuaCo(result, "C01");
            themNeuChuaCo(result, "D01");
        }

        // Fallback cuoi de khong lam DGNL = 0 chi vi NV dung to hop khong co dong quy doi rieng.
        themNeuChuaCo(result, "D01");
        themNeuChuaCo(result, "A01");
        themNeuChuaCo(result, "C01");
        themNeuChuaCo(result, "B00");
        return result;
    }

    private boolean toHopCoMon(ToHop toHop, String maMon) {
        if (toHop == null || toHop.getTohopId() == null || isBlank(maMon)) return false;
        try {
            for (ToHopMon thm : toHopDao.findMonByToHopId(toHop.getTohopId())) {
                if (thm.getMon() != null && maMon.equalsIgnoreCase(safe(thm.getMon().getMaMon()))) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    private int viTriUuTienToHop(ToHop th, List<String> thuTuUuTien) {
        if (th == null || th.getMaTohop() == null) return 999;
        String ma = th.getMaTohop().trim().toUpperCase(Locale.ROOT);
        int idx = thuTuUuTien.indexOf(ma);
        return idx >= 0 ? idx : 999;
    }

    private ToHop findToHopDaiDien(String maToHop) {
        if (isBlank(maToHop)) return null;
        String ma = maToHop.trim().toUpperCase(Locale.ROOT);
        if (!isMaToHopDgnlDaiDien(ma)) return null;

        ToHop cached = toHopDaiDienCache.get(ma);
        if (cached != null) return cached;

        ToHop th = toHopDao.findByMa(ma).orElse(null);
        if (th != null) {
            toHopDaiDienCache.put(ma, th);
        }
        return th;
    }

    private boolean isMaToHopDgnlDaiDien(String maToHop) {
        if (maToHop == null) return false;
        String ma = maToHop.trim().toUpperCase(Locale.ROOT);
        return "A01".equals(ma) || "B00".equals(ma) || "C01".equals(ma) || "D01".equals(ma);
    }

    private void themNeuChuaCo(List<String> list, String value) {
        if (value != null && !list.contains(value)) {
            list.add(value);
        }
    }

    private Integer layNganhId(NguyenVong nv, MaXetTuyenMap maMap) {
        if (nv != null && nv.getNganh() != null && nv.getNganh().getNganhId() != null) {
            return nv.getNganh().getNganhId();
        }
        if (maMap != null && maMap.getNganh() != null && maMap.getNganh().getNganhId() != null) {
            return maMap.getNganh().getNganhId();
        }
        if (nv != null && nv.getNganhToHop() != null
                && nv.getNganhToHop().getNganh() != null
                && nv.getNganhToHop().getNganh().getNganhId() != null) {
            return nv.getNganhToHop().getNganh().getNganhId();
        }
        return null;
    }

    private void themPhuongAnDGNLTheoNganh(List<PhuongAnDiem> result, Set<String> seen, NguyenVong nv, MaXetTuyenMap maMap) {
        Integer nganhId = layNganhId(nv, maMap);
        if (nganhId == null || nv == null || nv.getNganhToHop() == null) return;

        try {
            List<MaXetTuyenMap> maps = maXetTuyenMapDao.findByNganhIdWithDetails(nganhId);
            for (MaXetTuyenMap m : maps) {
                if (m == null || Boolean.FALSE.equals(m.getIsActive())) continue;
                PhuongThuc pt = m.getPhuongThuc();
                if (isPhuongThucDGNLHCM(pt)) {
                    // Dong DGNL trong DB co ma_xet_tuyen = NL1 va nganh_tohop_id = NULL,
                    // nen gan tam vao to hop NV hien tai; luc quy doi se tu chon bang dai dien A01/B00/C01/D01.
                    themPhuongAn(result, seen, pt, nv.getNganhToHop());
                }
            }
        } catch (Exception ignored) {
            // Khong co map DGNL thi bo qua, van tinh THPT/VSAT binh thuong.
        }
    }

    private List<PhuongAnDiem> taoDanhSachPhuongAnDiem(NguyenVong nv) {
        List<PhuongAnDiem> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        // Luon giu phuong an goc cua nguyen vong, thuong la THPT.
        themPhuongAn(result, seen, nv.getPhuongThuc(), nv.getNganhToHop());

        MaXetTuyenMap maMap = nv.getMaXetTuyenMap();
        if (maMap == null) {
            return result;
        }

        // Tranh loi LazyInitializationException: could not initialize proxy ... no Session.
        // Chi lay ID tu proxy, sau do nap lai MaXetTuyenMap bang DAO de co entity day du.
        try {
            Integer maXtId = maMap.getMaXettuyenId();
            if (maXtId != null) {
                MaXetTuyenMap loaded = maXetTuyenMapDao.findByIdWithDetails(maXtId);
                if (loaded != null) {
                    maMap = loaded;
                }
            }
        } catch (Exception ex) {
            return result;
        }

        if (isBlank(maMap.getMaXetTuyen())) {
            return result;
        }

        List<MaXetTuyenMap> maps = maXetTuyenMapDao.findByMaXetTuyenWithDetails(maMap.getMaXetTuyen());
        for (MaXetTuyenMap m : maps) {
            if (m == null || Boolean.FALSE.equals(m.getIsActive())) continue;

            if (nv.getNganh() != null && m.getNganh() != null
                    && !Objects.equals(nv.getNganh().getNganhId(), m.getNganh().getNganhId())) {
                continue;
            }

            PhuongThuc pt = m.getPhuongThuc();
            if (!laNguonDiemCanSoSanh(pt)) {
                continue;
            }

            NganhToHop nth = m.getNganhToHop() != null ? m.getNganhToHop() : nv.getNganhToHop();
            themPhuongAn(result, seen, pt, nth);
        }

        // DGNL trong DB duoc khai bao o cap nganh voi ma NL1, khong trung ma to hop cua NV.
        // Neu chi findByMaXetTuyen(C04/A00/...) thi se bo sot dong NL1.
        themPhuongAnDGNLTheoNganh(result, seen, nv, maMap);

        return result;
    }

    private void themPhuongAn(List<PhuongAnDiem> result, Set<String> seen, PhuongThuc pt, NganhToHop nth) {
        if (pt == null || nth == null || nth.getNganhTohopId() == null) return;
        if (!laNguonDiemCanSoSanh(pt)) return;

        String key = pt.getPhuongthucId() + "_" + nth.getNganhTohopId();
        if (seen.add(key)) {
            result.add(new PhuongAnDiem(pt, nth));
        }
    }

    private KetQuaDiem tinhDiemTheoPhuongAn(NguyenVong nvGoc, PhuongThuc pt, NganhToHop nth) {
        KetQuaDiem kq = new KetQuaDiem();
        kq.diemThxt = ZERO;
        kq.diemCong = ZERO;
        kq.diemUutien = ZERO;
        kq.diemXettuyen = ZERO;
        kq.coBangQuyDoi = false;
        kq.phuongThucDiemTotNhat = labelPhuongThuc(pt);

        if (nvGoc == null || nvGoc.getThiSinh() == null || pt == null || nth == null) {
            kq.ghiChu = "Phuong an diem khong hop le";
            return kq;
        }

        DiemThi diemThi = layDiemThiTheoNguon(nvGoc.getThiSinh(), pt);
        if (diemThi == null) {
            kq.ghiChu = "Khong co du lieu diem thi " + labelPhuongThuc(pt);
            return kq;
        }

        Optional<DiemCong> diemCong = Optional.empty();
        try {
            /*
             * Khi xét tuyển chỉ đọc điểm cộng đã được tạo sẵn.
             * Không tự tạo lại điểm cộng ở đây để tránh:
             * - Xét tuyển bị chậm
             * - DB tăng thêm row khi xét tuyển
             * - Dữ liệu điểm cộng phát sinh ngoài bước chuẩn bị dữ liệu
             *
             * Nếu không có bản ghi DiemCong thì coi như điểm cộng = 0.
             */
            diemCong = diemCongDao.findByThiSinhNganhToHopPhuongThuc(
                    nvGoc.getThiSinh().getThisinhId(),
                    nth.getNganhTohopId(),
                    pt.getPhuongthucId()
            );
        } catch (Exception e) {
            System.err.println("Khong lay duoc diem cong: " + e.getMessage());
        }

        NguyenVong nvTam = new NguyenVong();
        nvTam.setNguyenvongId(nvGoc.getNguyenvongId());
        nvTam.setThiSinh(nvGoc.getThiSinh());
        nvTam.setMaXetTuyenMap(nvGoc.getMaXetTuyenMap());
        nvTam.setNganh(nvGoc.getNganh());
        nvTam.setNganhToHop(nth);
        nvTam.setPhuongThuc(pt);
        nvTam.setThuTu(nvGoc.getThuTu());

        return tinhDiem(nvTam, diemThi, diemCong);
    }

    private DiemThi layDiemThiTheoNguon(ThiSinh ts, PhuongThuc pt) {
        if (ts == null || ts.getThisinhId() == null || pt == null || pt.getPhuongthucId() == null) {
            return null;
        }

        DiemThi diemChinh = diemThiDao.findByThiSinhAndPhuongThucWithDetails(
                ts.getThisinhId(),
                pt.getPhuongthucId(),
                NAM_TUYEN_SINH_MAC_DINH
        ).orElse(null);

        /*
         * NK la mon trong to hop THPT, nhung trong DB diem NK dang nam o phuong_thuc_id = 5.
         * Neu tinh THPT cho to hop co NK, ta ghep diem THPT + diem NK vao cung mot DiemThi tam.
         */
        if (!isPhuongThucTHPT(pt)) {
            return diemChinh;
        }

        PhuongThuc ptNk = phuongThucDao.findById((short) 5);
        DiemThi diemNk = null;
        if (ptNk != null) {
            diemNk = diemThiDao.findByThiSinhAndPhuongThucWithDetails(
                    ts.getThisinhId(),
                    ptNk.getPhuongthucId(),
                    NAM_TUYEN_SINH_MAC_DINH
            ).orElse(null);
        }

        if (diemChinh == null && diemNk == null) {
            return null;
        }
        if (diemNk == null) {
            return diemChinh;
        }

        DiemThi merged = new DiemThi();
        merged.setDiemthiId(diemChinh != null ? diemChinh.getDiemthiId() : diemNk.getDiemthiId());
        merged.setThiSinh(ts);
        merged.setPhuongThuc(pt);
        merged.setNamTuyensinh(NAM_TUYEN_SINH_MAC_DINH);
        merged.setDanhSachDiemChiTiet(new ArrayList<>());

        if (diemChinh != null && diemChinh.getDanhSachDiemChiTiet() != null) {
            merged.getDanhSachDiemChiTiet().addAll(diemChinh.getDanhSachDiemChiTiet());
        }
        if (diemNk.getDanhSachDiemChiTiet() != null) {
            merged.getDanhSachDiemChiTiet().addAll(diemNk.getDanhSachDiemChiTiet());
        }

        return merged;
    }

    private boolean laNguonDiemCanSoSanh(PhuongThuc pt) {
        return isPhuongThucTHPT(pt) || isPhuongThucVSAT(pt) || isPhuongThucDGNLHCM(pt);
    }


    /**
     * Tao diem cong toan bo theo batch/cache.
     *
     * Diem manh so voi luong cu trong DiemCongPanel:
     * - Neu clearOld = true: xoa nhanh bang xt_diemcong_chitiet va xt_diemcong bang DELETE bulk.
     * - Load truoc nguyen vong, chung chi, thanh tich vao Map theo thisinh_id.
     * - Tinh tong diem cong ngay trong RAM, khong goi recalculateTongHop tung ban ghi.
     * - Persist DiemCong kem chi tiet theo batch de giam so transaction.
     */
    public KetQuaTaoDiemCongBatch taoDiemCongToanBoBatch(List<ThiSinh> thiSinhList,
                                                          boolean onlyHasSource,
                                                          boolean onlyHasNguyenVong,
                                                          boolean clearOld,
                                                          int limit,
                                                          int batchSize) {
        KetQuaTaoDiemCongBatch kq = new KetQuaTaoDiemCongBatch();
        if (thiSinhList == null || thiSinhList.isEmpty()) {
            kq.addLog("Danh sach thi sinh rong.");
            return kq;
        }

        int safeBatchSize = batchSize > 0 ? batchSize : 500;

        if (clearOld) {
            diemCongDao.deleteAllFast();
            kq.clearedOldData = true;
        } else {
            kq.addLog("Canh bao: chua xoa du lieu cu. Neu DB da co diem cong, co the loi trung khoa unique.");
        }

        Map<Integer, List<NguyenVong>> nvByThiSinh = nhomNguyenVongTheoThiSinh();
        Map<Integer, List<ThiSinhChungChi>> chungChiByThiSinh = nhomChungChiHopLeTheoThiSinh();
        Map<Integer, List<ThiSinhThanhTich>> thanhTichByThiSinh = nhomThanhTichHopLeTheoThiSinh();
        Map<Integer, Boolean> toHopCoAnhCache = new HashMap<>();
        Map<Integer, List<ToHopMon>> monToHopCache = new HashMap<>();

        /*
         * Cache map ma xet tuyen de tao day du DiemCong cho cac phuong thuc co the duoc dung khi xet tuyen.
         * Truoc day batch chi lay nv.getPhuongThuc(), trong DB nguyen vong phan lon la THPT,
         * nen xt_diemcong chi sinh phuongthuc_id=THPT. Khi xet tuyen so sanh THPT/VSAT/DGNL,
         * DGNL/VSAT se khong co DiemCong san va co the bi tinh thieu diem cong hoac lam engine tu tao cham.
         */
        Map<Integer, MaXetTuyenMap> maXtByIdCache = new HashMap<>();
        Map<String, List<MaXetTuyenMap>> maXtByMaCache = new HashMap<>();
        Map<Integer, List<MaXetTuyenMap>> maXtByNganhCache = new HashMap<>();

        List<DiemCong> buffer = new ArrayList<>(safeBatchSize);

        for (ThiSinh ts : thiSinhList) {
            if (limit > 0 && kq.processed >= limit) break;
            if (ts == null || ts.getThisinhId() == null) {
                kq.skipped++;
                continue;
            }

            kq.processed++;
            Integer thisinhId = ts.getThisinhId();
            List<NguyenVong> nvs = nvByThiSinh.getOrDefault(thisinhId, Collections.emptyList());
            ThiSinh tsTinh = !nvs.isEmpty() && nvs.get(0).getThiSinh() != null ? nvs.get(0).getThiSinh() : ts;

            if (onlyHasNguyenVong && nvs.isEmpty()) {
                kq.skipped++;
                continue;
            }

            if (onlyHasSource && !coNguonDuLieuTinhDiemCached(tsTinh, chungChiByThiSinh, thanhTichByThiSinh)) {
                kq.skipped++;
                continue;
            }

            try {
                List<PhuongAnDiemCong> phuongAnList = taoPhuongAnDiemCongTuNguyenVong(
                        nvs,
                        null,
                        maXtByIdCache,
                        maXtByMaCache,
                        maXtByNganhCache
                );
                if (phuongAnList.isEmpty()) {
                    kq.skipped++;
                    continue;
                }

                int createdForThisinh = 0;
                for (PhuongAnDiemCong pa : phuongAnList) {
                    DiemCong dc = new DiemCong();
                    dc.setThiSinh(tsTinh);
                    dc.setNganhToHop(pa.nganhToHop);
                    dc.setPhuongThuc(pa.phuongThuc);
                    dc.setTongDiemChungChi(ZERO);
                    dc.setTongDiemUutienXt(ZERO);
                    dc.setTongDiemUutienQuyChe(ZERO);
                    dc.setTongDiemCong(ZERO);

                    List<DiemCongChiTiet> chiTietList = taoChiTietTuNguonGocCached(
                            tsTinh,
                            pa.nganhToHop,
                            pa.phuongThuc,
                            dc,
                            chungChiByThiSinh,
                            thanhTichByThiSinh,
                            toHopCoAnhCache,
                            monToHopCache
                    );

                    apDungTongHopDiemCong(dc, chiTietList);
                    dc.getChiTietList().clear();
                    for (DiemCongChiTiet ct : chiTietList) {
                        ct.setDiemCong(dc);
                        dc.getChiTietList().add(ct);
                    }

                    kq.soChiTiet += chiTietList.size();
                    kq.soDiemCong++;
                    createdForThisinh++;
                    buffer.add(dc);

                    if (buffer.size() >= safeBatchSize) {
                        diemCongDao.saveBatch(buffer, safeBatchSize);
                        buffer.clear();
                    }
                }

                if (createdForThisinh > 0) {
                    kq.success++;
                } else {
                    kq.skipped++;
                }
            } catch (Exception ex) {
                kq.error++;
                kq.addLog("Loi thi sinh " + safe(tsTinh.getCccd()) + " - " + safe(tsTinh.getHoVaTen()) + ": " + rootMessage(ex));
            }
        }

        if (!buffer.isEmpty()) {
            diemCongDao.saveBatch(buffer, safeBatchSize);
            buffer.clear();
        }

        return kq;
    }

    private static class PhuongAnDiemCong {
        final NganhToHop nganhToHop;
        final PhuongThuc phuongThuc;

        PhuongAnDiemCong(NganhToHop nganhToHop, PhuongThuc phuongThuc) {
            this.nganhToHop = nganhToHop;
            this.phuongThuc = phuongThuc;
        }
    }

    /**
     * Tao danh sach cap (nganh_tohop, phuong_thuc) can co DiemCong cho tung thi sinh.
     *
     * Diem quan trong:
     * - Khong chi lay nv.getPhuongThuc(), vi cot phuongthuc_id trong xt_nguyenvong thuong la THPT.
     * - Phai tao them cac phuong an VSAT/DGNL ma engine xet tuyen se dem ra so sanh trong tinhDiemTotNhat().
     * - Quy tac tao phuong an duoc canh theo logic taoDanhSachPhuongAnDiem():
     *   + phuong thuc goc cua nguyen vong;
     *   + cac dong xt_ma_xettuyen cung ma xet tuyen/cung nganh;
     *   + DGNL khai bao theo nganh bang ma NL1, gan vao to hop cua nguyen vong hien tai.
     */
    private List<PhuongAnDiemCong> taoPhuongAnDiemCongTuNguyenVong(List<NguyenVong> nvs,
                                                                    PhuongThuc ptMacDinh,
                                                                    Map<Integer, MaXetTuyenMap> maXtByIdCache,
                                                                    Map<String, List<MaXetTuyenMap>> maXtByMaCache,
                                                                    Map<Integer, List<MaXetTuyenMap>> maXtByNganhCache) {
        List<PhuongAnDiemCong> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        if (nvs == null) return result;

        for (NguyenVong nv : nvs) {
            if (nv == null || nv.getNganhToHop() == null || nv.getNganhToHop().getNganhTohopId() == null) continue;

            NganhToHop nthGoc = nv.getNganhToHop();

            // Neu caller truyen ptMacDinh thi chi tao theo phuong thuc do.
            if (ptMacDinh != null) {
                themPhuongAnDiemCong(result, seen, nthGoc, ptMacDinh);
                continue;
            }

            // 1. Luon tao theo phuong thuc goc cua nguyen vong, thuong la THPT.
            themPhuongAnDiemCong(result, seen, nthGoc, nv.getPhuongThuc());

            MaXetTuyenMap maMap = layMaXetTuyenMapDayDuCached(nv.getMaXetTuyenMap(), maXtByIdCache);
            String maXetTuyen = maMap != null ? maMap.getMaXetTuyen()
                    : (nv.getMaXetTuyenMap() != null ? nv.getMaXetTuyenMap().getMaXetTuyen() : null);

            // 2. Tao cac phuong thuc/tổ hợp duoc khai bao cung ma xet tuyen.
            // Vi du NV C04 co the co dong THPT/VSAT trong xt_ma_xettuyen.
            if (!isBlank(maXetTuyen)) {
                for (MaXetTuyenMap m : layMaXetTuyenMapsTheoMaCached(maXetTuyen, maXtByMaCache)) {
                    if (m == null || Boolean.FALSE.equals(m.getIsActive())) continue;
                    if (!cungNganhNguyenVong(nv, m)) continue;

                    PhuongThuc pt = m.getPhuongThuc();
                    if (!laNguonDiemCanSoSanh(pt)) continue;

                    NganhToHop nth = m.getNganhToHop() != null ? m.getNganhToHop() : nthGoc;
                    themPhuongAnDiemCong(result, seen, nth, pt);
                }
            }

            // 3. DGNL trong DB thuong khai bao theo nganh voi ma_xet_tuyen = NL1,
            // khong trung ma to hop/nguyen vong. Vi vay phai bo sung rieng theo nganh.
            Integer nganhId = layNganhId(nv, maMap);
            if (nganhId != null) {
                for (MaXetTuyenMap m : layMaXetTuyenMapsTheoNganhCached(nganhId, maXtByNganhCache)) {
                    if (m == null || Boolean.FALSE.equals(m.getIsActive())) continue;
                    PhuongThuc pt = m.getPhuongThuc();
                    if (isPhuongThucDGNLHCM(pt)) {
                        themPhuongAnDiemCong(result, seen, nthGoc, pt);
                    }
                }
            }
        }
        return result;
    }

    private void themPhuongAnDiemCong(List<PhuongAnDiemCong> result,
                                      Set<String> seen,
                                      NganhToHop nth,
                                      PhuongThuc pt) {
        if (nth == null || nth.getNganhTohopId() == null || pt == null || pt.getPhuongthucId() == null) return;
        if (!laNguonDiemCanSoSanh(pt)) return;

        String key = nth.getNganhTohopId() + "_" + pt.getPhuongthucId();
        if (seen.add(key)) {
            result.add(new PhuongAnDiemCong(nth, pt));
        }
    }

    private MaXetTuyenMap layMaXetTuyenMapDayDuCached(MaXetTuyenMap raw,
                                                       Map<Integer, MaXetTuyenMap> cache) {
        if (raw == null || raw.getMaXettuyenId() == null) return raw;
        if (cache == null) return raw;

        return cache.computeIfAbsent(raw.getMaXettuyenId(), id -> {
            try {
                MaXetTuyenMap loaded = maXetTuyenMapDao.findByIdWithDetails(id);
                return loaded != null ? loaded : raw;
            } catch (Exception ex) {
                return raw;
            }
        });
    }

    private List<MaXetTuyenMap> layMaXetTuyenMapsTheoMaCached(String maXetTuyen,
                                                               Map<String, List<MaXetTuyenMap>> cache) {
        if (isBlank(maXetTuyen)) return Collections.emptyList();
        String key = maXetTuyen.trim().toUpperCase(Locale.ROOT);
        if (cache == null) {
            return maXetTuyenMapDao.findByMaXetTuyenWithDetails(key);
        }
        return cache.computeIfAbsent(key, k -> {
            try {
                return maXetTuyenMapDao.findByMaXetTuyenWithDetails(k);
            } catch (Exception ex) {
                return Collections.emptyList();
            }
        });
    }

    private List<MaXetTuyenMap> layMaXetTuyenMapsTheoNganhCached(Integer nganhId,
                                                                  Map<Integer, List<MaXetTuyenMap>> cache) {
        if (nganhId == null) return Collections.emptyList();
        if (cache == null) {
            return maXetTuyenMapDao.findByNganhIdWithDetails(nganhId);
        }
        return cache.computeIfAbsent(nganhId, id -> {
            try {
                return maXetTuyenMapDao.findByNganhIdWithDetails(id);
            } catch (Exception ex) {
                return Collections.emptyList();
            }
        });
    }

    private boolean cungNganhNguyenVong(NguyenVong nv, MaXetTuyenMap m) {
        if (nv == null || m == null) return true;
        Integer nvNganhId = layNganhId(nv, null);
        Integer mapNganhId = m.getNganh() != null ? m.getNganh().getNganhId() : null;
        return nvNganhId == null || mapNganhId == null || Objects.equals(nvNganhId, mapNganhId);
    }

    private Map<Integer, List<NguyenVong>> nhomNguyenVongTheoThiSinh() {
        Map<Integer, List<NguyenVong>> map = new HashMap<>();
        List<NguyenVong> all = nguyenVongDao.findAllForXetTuyen();
        for (NguyenVong nv : all) {
            if (nv == null || nv.getThiSinh() == null || nv.getThiSinh().getThisinhId() == null) continue;
            map.computeIfAbsent(nv.getThiSinh().getThisinhId(), k -> new ArrayList<>()).add(nv);
        }
        return map;
    }

    private Map<Integer, List<ThiSinhChungChi>> nhomChungChiHopLeTheoThiSinh() {
        Map<Integer, List<ThiSinhChungChi>> map = new HashMap<>();
        for (ThiSinhChungChi cc : thiSinhChungChiService.findAll()) {
            if (cc == null || cc.getThiSinh() == null || cc.getThiSinh().getThisinhId() == null) continue;
            if (!Boolean.TRUE.equals(cc.getIsHopLe())) continue;
            if (!"DA_XAC_MINH".equalsIgnoreCase(safe(cc.getTrangThaiXacMinh()))) continue;
            if (!isChungChiConHan(cc)) continue;
            map.computeIfAbsent(cc.getThiSinh().getThisinhId(), k -> new ArrayList<>()).add(cc);
        }
        return map;
    }

    private Map<Integer, List<ThiSinhThanhTich>> nhomThanhTichHopLeTheoThiSinh() {
        Map<Integer, List<ThiSinhThanhTich>> map = new HashMap<>();
        for (ThiSinhThanhTich tt : thiSinhThanhTichService.findAll()) {
            if (tt == null || tt.getThiSinh() == null || tt.getThiSinh().getThisinhId() == null) continue;
            if (!Boolean.TRUE.equals(tt.getIsHopLe())) continue;
            if (!"DA_XAC_MINH".equalsIgnoreCase(safe(tt.getTrangThaiXacMinh()))) continue;
            map.computeIfAbsent(tt.getThiSinh().getThisinhId(), k -> new ArrayList<>()).add(tt);
        }
        return map;
    }

    private boolean coNguonDuLieuTinhDiemCached(ThiSinh ts,
                                                Map<Integer, List<ThiSinhChungChi>> chungChiByThiSinh,
                                                Map<Integer, List<ThiSinhThanhTich>> thanhTichByThiSinh) {
        if (ts == null || ts.getThisinhId() == null) return false;
        if (!chungChiByThiSinh.getOrDefault(ts.getThisinhId(), Collections.emptyList()).isEmpty()) return true;
        if (!thanhTichByThiSinh.getOrDefault(ts.getThisinhId(), Collections.emptyList()).isEmpty()) return true;

        BigDecimal khuVuc = ts.getKhuVucUutien() != null ? safe(ts.getKhuVucUutien().getMucDiem()) : ZERO;
        BigDecimal doiTuong = ts.getDoiTuongUutien() != null ? safe(ts.getDoiTuongUutien().getMucDiem()) : ZERO;
        return khuVuc.add(doiTuong).compareTo(ZERO) > 0;
    }

    private List<DiemCongChiTiet> taoChiTietTuNguonGocCached(ThiSinh ts,
                                                              NganhToHop nth,
                                                              PhuongThuc pt,
                                                              DiemCong dc,
                                                              Map<Integer, List<ThiSinhChungChi>> chungChiByThiSinh,
                                                              Map<Integer, List<ThiSinhThanhTich>> thanhTichByThiSinh,
                                                              Map<Integer, Boolean> toHopCoAnhCache,
                                                              Map<Integer, List<ToHopMon>> monToHopCache) {
        List<DiemCongChiTiet> list = new ArrayList<>();
        if (ts == null || ts.getThisinhId() == null || nth == null || pt == null) return list;

        short thuTu = 1;

        if (ts.getKhuVucUutien() != null
                && ts.getKhuVucUutien().getMucDiem() != null
                && ts.getKhuVucUutien().getMucDiem().compareTo(ZERO) > 0) {
            DiemCongChiTiet ct = new DiemCongChiTiet();
            ct.setDiemCong(dc);
            ct.setLoaiNguon(DiemCongChiTiet.LoaiNguon.UUTIEN_KHUVUC);
            ct.setMaNguon(ts.getKhuVucUutien().getMaKhuVuc());
            ct.setTenNguon(ts.getKhuVucUutien().getTenKhuvuc());
            ct.setGiaTriGoc(ts.getKhuVucUutien().getMucDiem().toPlainString());
            ct.setDiemQuyDoi(ZERO);
            ct.setDiemCongGiaTri(ts.getKhuVucUutien().getMucDiem());
            ct.setThuTuUuTien(thuTu++);
            ct.setIsApDung(true);
            ct.setGhiChu("Uu tien quy che theo khu vuc");
            list.add(ct);
        }

        if (ts.getDoiTuongUutien() != null
                && ts.getDoiTuongUutien().getMucDiem() != null
                && ts.getDoiTuongUutien().getMucDiem().compareTo(ZERO) > 0) {
            DiemCongChiTiet ct = new DiemCongChiTiet();
            ct.setDiemCong(dc);
            ct.setLoaiNguon(DiemCongChiTiet.LoaiNguon.UUTIEN_DOITUONG);
            ct.setMaNguon(ts.getDoiTuongUutien().getMaDoituong());
            ct.setTenNguon(ts.getDoiTuongUutien().getTenDoituong());
            ct.setGiaTriGoc(ts.getDoiTuongUutien().getMucDiem().toPlainString());
            ct.setDiemQuyDoi(ZERO);
            ct.setDiemCongGiaTri(ts.getDoiTuongUutien().getMucDiem());
            ct.setThuTuUuTien(thuTu++);
            ct.setIsApDung(true);
            ct.setGhiChu("Uu tien quy che theo doi tuong");
            list.add(ct);
        }

        List<ThiSinhChungChi> chungChiList = chungChiByThiSinh.getOrDefault(ts.getThisinhId(), Collections.emptyList());
        for (ThiSinhChungChi cc : chungChiList) {
            BigDecimal diemQdAnh = tinhDiemQuyDoiMonAnhSGU(cc);
            BigDecimal diemCong = tinhDiemCongKhuyenKhichChungChiSGU(cc);

            boolean toHopCoAnh = toHopCoMonAnhCached(nth.getToHop(), toHopCoAnhCache, monToHopCache);
            boolean laPT2 = isPhuongThucDGNLHCM(pt);
            boolean laPT3or4 = isPhuongThucVSATHoacTHPT(pt);

            if (laPT2) {
                if (diemCong.compareTo(ZERO) > 0) {
                    DiemCongChiTiet ct = taoChiTietChungChi(dc, cc, pt, thuTu++);
                    ct.setDiemQuyDoi(ZERO);
                    ct.setDiemCongGiaTri(diemCong);
                    ct.setGhiChu("PT2 - cong diem khuyen khich tu chung chi");
                    list.add(ct);
                }
                continue;
            }

            if (laPT3or4) {
                if (toHopCoAnh) {
                    if (diemQdAnh.compareTo(ZERO) > 0) {
                        DiemCongChiTiet ct = taoChiTietChungChi(dc, cc, pt, thuTu++);
                        ct.setDiemQuyDoi(diemQdAnh);
                        ct.setDiemCongGiaTri(ZERO);
                        ct.setGhiChu("PT3/PT4 - quy doi diem mon Anh tu chung chi");
                        list.add(ct);
                    }
                } else if (diemCong.compareTo(ZERO) > 0) {
                    DiemCongChiTiet ct = taoChiTietChungChi(dc, cc, pt, thuTu++);
                    ct.setDiemQuyDoi(ZERO);
                    ct.setDiemCongGiaTri(diemCong);
                    ct.setGhiChu("PT3/PT4 - to hop khong co mon Anh, cong diem khuyen khich");
                    list.add(ct);
                }
            }
        }

        List<ThiSinhThanhTich> thanhTichList = thanhTichByThiSinh.getOrDefault(ts.getThisinhId(), Collections.emptyList());
        for (ThiSinhThanhTich tt : thanhTichList) {
            DiemCongChiTiet.LoaiNguon loaiNguon = xacDinhLoaiNguonThanhTich(tt);
            if (loaiNguon == null) continue;

            boolean monTrungToHop = monDatGiaiThuocToHopCached(tt, nth.getToHop(), monToHopCache);
            BigDecimal diemCong = tinhDiemCongThanhTichSGU(tt, monTrungToHop);
            if (diemCong.compareTo(ZERO) <= 0) continue;

            DiemCongChiTiet ct = new DiemCongChiTiet();
            ct.setDiemCong(dc);
            ct.setLoaiNguon(loaiNguon);
            ct.setMaNguon(tt.getNhomThanhTich());
            ct.setTenNguon(tt.getTenThanhTich() != null ? tt.getTenThanhTich() : tt.getNhomThanhTich());
            ct.setCapApDung(pt != null ? pt.getMaPhuongthuc() : null);
            ct.setMonLienQuan(tt.getMonDatGiai());
            ct.setGiaTriGoc(tt.getLoaiGiai());
            ct.setDiemQuyDoi(ZERO);
            ct.setDiemCongGiaTri(diemCong);
            ct.setThuTuUuTien(thuTu++);
            ct.setIsApDung(true);
            ct.setGhiChu(monTrungToHop
                    ? "Thanh tich co mon dat giai thuoc to hop xet tuyen"
                    : "Thanh tich khong co mon dat giai trong to hop xet tuyen");
            list.add(ct);
        }

        return list;
    }

    private boolean toHopCoMonAnhCached(ToHop toHop,
                                        Map<Integer, Boolean> toHopCoAnhCache,
                                        Map<Integer, List<ToHopMon>> monToHopCache) {
        if (toHop == null || toHop.getTohopId() == null) return false;
        return toHopCoAnhCache.computeIfAbsent(toHop.getTohopId(), id -> {
            for (ToHopMon thm : layMonToHopCached(toHop, monToHopCache)) {
                if (thm.getMon() != null && laMonAnh(thm.getMon())) {
                    return true;
                }
            }
            return false;
        });
    }

    private List<ToHopMon> layMonToHopCached(ToHop toHop, Map<Integer, List<ToHopMon>> monToHopCache) {
        if (toHop == null || toHop.getTohopId() == null) return Collections.emptyList();
        return monToHopCache.computeIfAbsent(toHop.getTohopId(), id -> toHopDao.findMonByToHopId(id));
    }

    private boolean monDatGiaiThuocToHopCached(ThiSinhThanhTich tt,
                                                ToHop toHop,
                                                Map<Integer, List<ToHopMon>> monToHopCache) {
        if (tt == null || toHop == null || isBlank(tt.getMonDatGiai())) return false;

        String monDatGiai = normalize(tt.getMonDatGiai());
        if (containsAny(monDatGiai, "KHAC")) return false;

        for (ToHopMon thm : layMonToHopCached(toHop, monToHopCache)) {
            if (thm.getMon() == null) continue;

            String maMon = normalize(thm.getMon().getMaMon());
            String tenMon = normalize(thm.getMon().getTenMon());

            if (monDatGiai.equals(maMon) || monDatGiai.equals(tenMon)) return true;
            if ((containsAny(monDatGiai, "TOAN") || monDatGiai.equals("TO")) && maMon.equals("TO")) return true;
            if ((containsAny(monDatGiai, "VATLI", "VATLY") || monDatGiai.equals("LI")) && maMon.equals("LI")) return true;
            if ((containsAny(monDatGiai, "HOAHOC") || monDatGiai.equals("HO")) && maMon.equals("HO")) return true;
            if ((containsAny(monDatGiai, "SINHHOC") || monDatGiai.equals("SI")) && maMon.equals("SI")) return true;
            if ((containsAny(monDatGiai, "NGUVAN", "VAN") || monDatGiai.equals("VA")) && maMon.equals("VA")) return true;
            if ((containsAny(monDatGiai, "LICHSU") || monDatGiai.equals("SU")) && maMon.equals("SU")) return true;
            if ((containsAny(monDatGiai, "DIALI", "DIALY") || monDatGiai.equals("DI")) && maMon.equals("DI")) return true;
            if ((containsAny(monDatGiai, "TIENGANH", "NGOAINGU") || monDatGiai.equals("N1")) && maMon.equals("N1")) return true;
            if (containsAny(monDatGiai, "GDKTPL", "KTPL") && maMon.equals("KTPL")) return true;
        }

        return false;
    }

    private void apDungTongHopDiemCong(DiemCong dc, List<DiemCongChiTiet> chiTietList) {
        if (dc == null) return;

        BigDecimal tongChungChi = ZERO;
        BigDecimal tongUuTienXt = ZERO;
        BigDecimal tongUuTienQc = ZERO;

        if (chiTietList != null) {
            for (DiemCongChiTiet ct : chiTietList) {
                if (ct == null || Boolean.FALSE.equals(ct.getIsApDung()) || ct.getLoaiNguon() == null) continue;

                BigDecimal diemCong = safe(ct.getDiemCongGiaTri());
                switch (ct.getLoaiNguon()) {
                    case CC_NGOAI_NGU:
                        tongChungChi = tongChungChi.add(diemCong);
                        break;
                    case UUTIEN_KHUVUC:
                    case UUTIEN_DOITUONG:
                        tongUuTienQc = tongUuTienQc.add(diemCong);
                        break;
                    case UTXT_HSG_QUOCGIA:
                    case UTXT_HSG_TINH:
                    case UTXT_KHKT:
                    case UTXT_NGHE_THUAT:
                        tongUuTienXt = tongUuTienXt.add(diemCong);
                        break;
                    default:
                        break;
                }
            }
        }

        BigDecimal tongThucTe = tongChungChi.add(tongUuTienXt).add(tongUuTienQc);
        BigDecimal tongSauTran = tongThucTe.min(new BigDecimal("3.00"));

        dc.setTongDiemChungChi(tongChungChi);
        dc.setTongDiemUutienXt(tongUuTienXt);
        dc.setTongDiemUutienQuyChe(tongUuTienQc);
        dc.setTongDiemCong(tongSauTran);
        dc.setGhiChuTong(tongThucTe.compareTo(new BigDecimal("3.00")) > 0
                ? "Tong diem cong thuc te = " + tongThucTe.toPlainString() + ", ap tran SGU = 3.00"
                : null);
    }

    private String rootMessage(Throwable ex) {
        if (ex == null) return "Khong ro loi";
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : root.toString();
    }

    /**
     * Tao / cap nhat diem cong tu dong cho mot thi sinh theo cac nguyen vong.
     */
    public List<DiemCong> taoDiemCongTuDong(ThiSinh ts, PhuongThuc ptMacDinh) {
        List<DiemCong> list = new ArrayList<>();
        if (ts == null || ts.getThisinhId() == null) return list;

        List<NguyenVong> nvs = nguyenVongDao.findByThiSinhId(ts.getThisinhId());
        Set<String> uniquePairs = new LinkedHashSet<>();

        for (NguyenVong nv : nvs) {
            if (nv.getNganhToHop() == null) continue;

            /*
             * Neu ptMacDinh != null thi tao diem cong theo phuong thuc dang can tinh
             * (THPT / VSAT / DGNL), khong bi khoa theo phuongThuc cua nguyen vong.
             * Vi file NguyenVong.xlsx khong co cot phuong thuc, hau het NV dang luu THPT,
             * nhung khi so sanh diem tot nhat ta van can DiemCong cho VSAT/DGNL.
             */
            Short ptId = ptMacDinh != null
                    ? ptMacDinh.getPhuongthucId()
                    : (nv.getPhuongThuc() != null ? nv.getPhuongThuc().getPhuongthucId() : null);
            if (ptId == null) continue;

            uniquePairs.add(nv.getNganhToHop().getNganhTohopId() + "_" + ptId);
        }

        for (String key : uniquePairs) {
            String[] parts = key.split("_");
            Integer nthId = Integer.parseInt(parts[0]);
            Short ptId = Short.parseShort(parts[1]);

            NganhToHop nth = nganhToHopDao.findById(nthId);
            PhuongThuc pt = phuongThucDao.findById(ptId);
            if (nth == null || pt == null) continue;

            Optional<DiemCong> opt = diemCongDao.findByThiSinhNganhToHopPhuongThuc(
                    ts.getThisinhId(), nthId, ptId
            );

            DiemCong dc;
            if (opt.isPresent()) {
                dc = opt.get();
                dc.setTongDiemChungChi(ZERO);
                dc.setTongDiemUutienXt(ZERO);
                dc.setTongDiemUutienQuyChe(ZERO);
                dc.setTongDiemCong(ZERO);
                dc.setGhiChuTong(null);
                diemCongService.update(dc);
                diemCongChiTietService.deleteByDiemCongId(dc.getDiemcongId());
            } else {
                dc = new DiemCong();
                dc.setThiSinh(ts);
                dc.setNganhToHop(nth);
                dc.setPhuongThuc(pt);
                dc.setTongDiemChungChi(ZERO);
                dc.setTongDiemUutienXt(ZERO);
                dc.setTongDiemUutienQuyChe(ZERO);
                dc.setTongDiemCong(ZERO);
                dc = diemCongService.save(dc);
            }

            List<DiemCongChiTiet> chiTietList = taoChiTietTuNguonGoc(ts, nth, pt, dc);
            apDungTongHopDiemCong(dc, chiTietList);
            diemCongService.update(dc);

            for (DiemCongChiTiet ct : chiTietList) {
                diemCongChiTietService.save(ct);
            }

            list.add(dc);
        }

        return list;
    }

    private Optional<DiemCong> taoDiemCongChoPhuongAn(ThiSinh ts, NganhToHop nth, PhuongThuc pt) {
        if (ts == null || ts.getThisinhId() == null || nth == null || nth.getNganhTohopId() == null
                || pt == null || pt.getPhuongthucId() == null) {
            return Optional.empty();
        }

        Optional<DiemCong> opt = diemCongDao.findByThiSinhNganhToHopPhuongThuc(
                ts.getThisinhId(),
                nth.getNganhTohopId(),
                pt.getPhuongthucId()
        );

        DiemCong dc;
        if (opt.isPresent()) {
            dc = opt.get();
            dc.setTongDiemChungChi(ZERO);
            dc.setTongDiemUutienXt(ZERO);
            dc.setTongDiemUutienQuyChe(ZERO);
            dc.setTongDiemCong(ZERO);
            dc.setGhiChuTong(null);
            diemCongService.update(dc);
            diemCongChiTietService.deleteByDiemCongId(dc.getDiemcongId());
        } else {
            dc = new DiemCong();
            dc.setThiSinh(ts);
            dc.setNganhToHop(nth);
            dc.setPhuongThuc(pt);
            dc.setTongDiemChungChi(ZERO);
            dc.setTongDiemUutienXt(ZERO);
            dc.setTongDiemUutienQuyChe(ZERO);
            dc.setTongDiemCong(ZERO);
            dc = diemCongService.save(dc);
        }

        List<DiemCongChiTiet> chiTietList = taoChiTietTuNguonGoc(ts, nth, pt, dc);
        apDungTongHopDiemCong(dc, chiTietList);
        diemCongService.update(dc);

        for (DiemCongChiTiet ct : chiTietList) {
            diemCongChiTietService.save(ct);
        }

        return Optional.of(dc);
    }

    private List<DiemCongChiTiet> taoChiTietTuNguonGoc(ThiSinh ts, NganhToHop nth, PhuongThuc pt, DiemCong dc) {
        List<DiemCongChiTiet> list = new ArrayList<>();
        short thuTu = 1;

        // 1. Uu tien quy che - khu vuc
        if (ts.getKhuVucUutien() != null
                && ts.getKhuVucUutien().getMucDiem() != null
                && ts.getKhuVucUutien().getMucDiem().compareTo(ZERO) > 0) {

            DiemCongChiTiet ct = new DiemCongChiTiet();
            ct.setDiemCong(dc);
            ct.setLoaiNguon(DiemCongChiTiet.LoaiNguon.UUTIEN_KHUVUC);
            ct.setMaNguon(ts.getKhuVucUutien().getMaKhuVuc());
            ct.setTenNguon(ts.getKhuVucUutien().getTenKhuvuc());
            ct.setGiaTriGoc(ts.getKhuVucUutien().getMucDiem().toPlainString());
            ct.setDiemQuyDoi(ZERO);
            ct.setDiemCongGiaTri(ts.getKhuVucUutien().getMucDiem());
            ct.setThuTuUuTien(thuTu++);
            ct.setIsApDung(true);
            ct.setGhiChu("Uu tien quy che theo khu vuc");
            list.add(ct);
        }

        // 2. Uu tien quy che - doi tuong
        if (ts.getDoiTuongUutien() != null
                && ts.getDoiTuongUutien().getMucDiem() != null
                && ts.getDoiTuongUutien().getMucDiem().compareTo(ZERO) > 0) {

            DiemCongChiTiet ct = new DiemCongChiTiet();
            ct.setDiemCong(dc);
            ct.setLoaiNguon(DiemCongChiTiet.LoaiNguon.UUTIEN_DOITUONG);
            ct.setMaNguon(ts.getDoiTuongUutien().getMaDoituong());
            ct.setTenNguon(ts.getDoiTuongUutien().getTenDoituong());
            ct.setGiaTriGoc(ts.getDoiTuongUutien().getMucDiem().toPlainString());
            ct.setDiemQuyDoi(ZERO);
            ct.setDiemCongGiaTri(ts.getDoiTuongUutien().getMucDiem());
            ct.setThuTuUuTien(thuTu++);
            ct.setIsApDung(true);
            ct.setGhiChu("Uu tien quy che theo doi tuong");
            list.add(ct);
        }

        // 3. Chung chi ngoai ngu
        List<ThiSinhChungChi> chungChiList = thiSinhChungChiService.findHopLeByThiSinhId(ts.getThisinhId());
        for (ThiSinhChungChi cc : chungChiList) {
            if (!isChungChiConHan(cc)) continue;

            BigDecimal diemQdAnh = tinhDiemQuyDoiMonAnhSGU(cc);
            BigDecimal diemCong = tinhDiemCongKhuyenKhichChungChiSGU(cc);

            boolean toHopCoAnh = toHopCoMonAnh(nth.getToHop());
            boolean laPT2 = isPhuongThucDGNLHCM(pt);
            boolean laPT3or4 = isPhuongThucVSATHoacTHPT(pt);

            if (laPT2) {
                if (diemCong.compareTo(ZERO) > 0) {
                    DiemCongChiTiet ct = taoChiTietChungChi(dc, cc, pt, thuTu++);
                    ct.setDiemQuyDoi(ZERO);
                    ct.setDiemCongGiaTri(diemCong);
                    ct.setGhiChu("PT2 - cong diem khuyen khich tu chung chi");
                    list.add(ct);
                }
                continue;
            }

            if (laPT3or4) {
                if (toHopCoAnh) {
                    if (diemQdAnh.compareTo(ZERO) > 0) {
                        DiemCongChiTiet ct = taoChiTietChungChi(dc, cc, pt, thuTu++);
                        ct.setDiemQuyDoi(diemQdAnh);
                        ct.setDiemCongGiaTri(ZERO);
                        ct.setGhiChu("PT3/PT4 - quy doi diem mon Anh tu chung chi");
                        list.add(ct);
                    }
                } else {
                    if (diemCong.compareTo(ZERO) > 0) {
                        DiemCongChiTiet ct = taoChiTietChungChi(dc, cc, pt, thuTu++);
                        ct.setDiemQuyDoi(ZERO);
                        ct.setDiemCongGiaTri(diemCong);
                        ct.setGhiChu("PT3/PT4 - to hop khong co mon Anh, cong diem khuyen khich");
                        list.add(ct);
                    }
                }
            }
        }

        // 4. Thanh tich uu tien xet tuyen
        List<ThiSinhThanhTich> thanhTichList = thiSinhThanhTichService.findHopLeByThiSinhId(ts.getThisinhId());
        for (ThiSinhThanhTich tt : thanhTichList) {
            DiemCongChiTiet.LoaiNguon loaiNguon = xacDinhLoaiNguonThanhTich(tt);
            if (loaiNguon == null) continue;

            boolean monTrungToHop = monDatGiaiThuocToHop(tt, nth.getToHop());
            BigDecimal diemCong = tinhDiemCongThanhTichSGU(tt, monTrungToHop);
            if (diemCong.compareTo(ZERO) <= 0) continue;

            DiemCongChiTiet ct = new DiemCongChiTiet();
            ct.setDiemCong(dc);
            ct.setLoaiNguon(loaiNguon);
            ct.setMaNguon(tt.getNhomThanhTich());
            ct.setTenNguon(tt.getTenThanhTich() != null ? tt.getTenThanhTich() : tt.getNhomThanhTich());
            ct.setCapApDung(pt != null ? pt.getMaPhuongthuc() : null);
            ct.setMonLienQuan(tt.getMonDatGiai());
            ct.setGiaTriGoc(tt.getLoaiGiai());
            ct.setDiemQuyDoi(ZERO);
            ct.setDiemCongGiaTri(diemCong);
            ct.setThuTuUuTien(thuTu++);
            ct.setIsApDung(true);
            ct.setGhiChu(monTrungToHop
                    ? "Thanh tich co mon dat giai thuoc to hop xet tuyen"
                    : "Thanh tich khong co mon dat giai trong to hop xet tuyen");
            list.add(ct);
        }

        return list;
    }

    private DiemCongChiTiet taoChiTietChungChi(DiemCong dc, ThiSinhChungChi cc, PhuongThuc pt, short thuTu) {
        DiemCongChiTiet ct = new DiemCongChiTiet();
        ct.setDiemCong(dc);
        ct.setLoaiNguon(DiemCongChiTiet.LoaiNguon.CC_NGOAI_NGU);
        ct.setMaNguon(cc.getLoaiChungChi());
        ct.setTenNguon(cc.getTenChungChi() != null ? cc.getTenChungChi() : cc.getLoaiChungChi());
        ct.setCapApDung(pt != null ? pt.getMaPhuongthuc() : null);
        ct.setMonLienQuan("N1");
        ct.setGiaTriGoc(cc.getDiemGoc() != null ? cc.getDiemGoc().toPlainString() : cc.getBacChungChi());
        ct.setThuTuUuTien(thuTu);
        ct.setIsApDung(true);
        return ct;
    }

    private DiemCongChiTiet.LoaiNguon xacDinhLoaiNguonThanhTich(ThiSinhThanhTich tt) {
        String nhom = normalize(tt.getNhomThanhTich());
        String cap = normalize(tt.getCapThanhTich());
        String linhVuc = normalize(tt.getLinhVuc());

        if (containsAny(nhom, "HOCSINHGIOI", "HSG")) {
            if (containsAny(cap, "QUOCGIA")) return DiemCongChiTiet.LoaiNguon.UTXT_HSG_QUOCGIA;
            if (containsAny(cap, "TINH", "THANHPHO")) return DiemCongChiTiet.LoaiNguon.UTXT_HSG_TINH;
        }

        if (containsAny(nhom, "KHOAHOCKYTHUAT", "KHKT") || containsAny(linhVuc, "KHOAHOCKYTHUAT", "KHKT")) {
            return DiemCongChiTiet.LoaiNguon.UTXT_KHKT;
        }

        if (containsAny(nhom, "NGHETHUAT", "THETHAO") || containsAny(linhVuc, "NGHETHUAT", "THETHAO")) {
            return DiemCongChiTiet.LoaiNguon.UTXT_NGHE_THUAT;
        }

        return null;
    }

    /**
     * Uu tien doc tu ghi_chu import truoc.
     * Neu khong co thi fallback ve hard-code de tranh vo logic cu.
     */
    private BigDecimal tinhDiemQuyDoiMonAnhSGU(ThiSinhChungChi cc) {
        if (cc == null) return ZERO;

        BigDecimal tuGhiChu = parseBigDecimalMeta(cc.getGhiChu(), "excel_diem_quy_doi");
        if (tuGhiChu != null) return tuGhiChu;

        if (cc.getDiemGoc() == null) return ZERO;

        String loai = normalize(cc.getLoaiChungChi());
        BigDecimal diem = cc.getDiemGoc();

        if (containsAny(loai, "IELTS")) {
            if (diem.compareTo(new BigDecimal("7.0")) >= 0) return new BigDecimal("10.0");
            if (diem.compareTo(new BigDecimal("5.5")) >= 0) return new BigDecimal("9.0");
            if (diem.compareTo(new BigDecimal("4.0")) >= 0) return new BigDecimal("8.0");
        }

        return ZERO;
    }

    /**
     * Uu tien doc tu ghi_chu import truoc.
     * Neu khong co thi fallback ve hard-code de tranh vo logic cu.
     */
    private BigDecimal tinhDiemCongKhuyenKhichChungChiSGU(ThiSinhChungChi cc) {
        if (cc == null) return ZERO;

        BigDecimal tuGhiChu = parseBigDecimalMeta(cc.getGhiChu(), "excel_diem_cong");
        if (tuGhiChu != null) return tuGhiChu;

        if (cc.getDiemGoc() == null) return ZERO;

        String loai = normalize(cc.getLoaiChungChi());
        BigDecimal diem = cc.getDiemGoc();

        if (containsAny(loai, "IELTS")) {
            if (diem.compareTo(new BigDecimal("7.0")) >= 0) return new BigDecimal("2.0");
            if (diem.compareTo(new BigDecimal("5.5")) >= 0) return new BigDecimal("1.5");
            if (diem.compareTo(new BigDecimal("4.0")) >= 0) return new BigDecimal("1.0");
        }

        return ZERO;
    }

    /**
     * Uu tien doc tu ghi_chu import truoc.
     * Neu khong co thi fallback ve hard-code de tranh vo logic cu.
     */
    private BigDecimal tinhDiemCongThanhTichSGU(ThiSinhThanhTich tt, boolean monTrungToHop) {
        if (tt == null) return ZERO;

        BigDecimal tuGhiChu = monTrungToHop
                ? parseBigDecimalMeta(tt.getGhiChu(), "excel_cong_mon")
                : parseBigDecimalMeta(tt.getGhiChu(), "excel_cong_khong_mon");
        if (tuGhiChu != null) return tuGhiChu;

        String nhom = normalize(tt.getNhomThanhTich());
        String cap = normalize(tt.getCapThanhTich());
        String loai = normalize(tt.getLoaiGiai());
        String linhVuc = normalize(tt.getLinhVuc());

        if (containsAny(nhom, "HOCSINHGIOI", "HSG") && containsAny(cap, "QUOCGIA")) {
            if (containsAny(loai, "GIAINHI", "NHI")) return monTrungToHop ? new BigDecimal("2.0") : new BigDecimal("0.75");
            if (containsAny(loai, "GIAIBA", "BA")) return monTrungToHop ? new BigDecimal("1.5") : new BigDecimal("0.50");
            if (containsAny(loai, "KHUYENKHICH", "KK")) return monTrungToHop ? new BigDecimal("1.0") : ZERO;
        }

        if (containsAny(nhom, "HOCSINHGIOI", "HSG") && containsAny(cap, "TINH", "THANHPHO")) {
            if (containsAny(loai, "GIAINHAT", "NHAT")) return monTrungToHop ? new BigDecimal("1.0") : new BigDecimal("0.25");
            if (containsAny(loai, "GIAINHI", "NHI")) return monTrungToHop ? new BigDecimal("0.75") : ZERO;
            if (containsAny(loai, "GIAIBA", "BA")) return monTrungToHop ? new BigDecimal("0.50") : ZERO;
        }

        if (containsAny(nhom, "KHOAHOCKYTHUAT", "KHKT") || containsAny(linhVuc, "KHOAHOCKYTHUAT", "KHKT")) {
            if (containsAny(cap, "QUOCGIA")) {
                if (containsAny(loai, "GIAINHAT", "NHAT")) return monTrungToHop ? new BigDecimal("2.0") : new BigDecimal("0.75");
                if (containsAny(loai, "GIAINHI", "NHI")) return monTrungToHop ? new BigDecimal("1.5") : new BigDecimal("0.50");
                if (containsAny(loai, "GIAIBA", "BA")) return monTrungToHop ? new BigDecimal("1.0") : ZERO;
                if (containsAny(loai, "GIAITU", "TU")) return monTrungToHop ? new BigDecimal("0.5") : ZERO;
            }
        }

        if (containsAny(nhom, "NGHETHUAT", "THETHAO") || containsAny(linhVuc, "NGHETHUAT", "THETHAO")) {
            if (containsAny(cap, "QUOCGIA")) return new BigDecimal("1.0");
            if (containsAny(cap, "TINH", "THANHPHO")) return new BigDecimal("0.5");
        }

        return ZERO;
    }

    private BigDecimal layDiemQuyDoiTiengAnhTuDiemCong(DiemCong dc) {
        if (dc == null || dc.getDiemcongId() == null) return ZERO;

        BigDecimal best = ZERO;
        List<DiemCongChiTiet> list = diemCongChiTietService.findAppliedByDiemCongId(dc.getDiemcongId());
        for (DiemCongChiTiet ct : list) {
            if (ct.getLoaiNguon() != DiemCongChiTiet.LoaiNguon.CC_NGOAI_NGU) continue;
            if (ct.getDiemQuyDoi() != null && ct.getDiemQuyDoi().compareTo(best) > 0) {
                best = ct.getDiemQuyDoi();
            }
        }
        return best;
    }

    /**
     * Tim diem DGNL tong da import vao mon NL1.
     */
    private BigDecimal timDiemDgnlTuChiTiet(Map<Integer, DiemThiChiTiet> diemTheoMon) {
        if (diemTheoMon == null || diemTheoMon.isEmpty()) return null;

        for (DiemThiChiTiet ct : diemTheoMon.values()) {
            if (ct == null || ct.getMon() == null) continue;

            String maMon = normalize(ct.getMon().getMaMon());
            if (!"NL1".equals(maMon)) continue;

            // DGNL phai lay diem_goc (diem tong NL1) de dua vao bang quy doi theo to hop.
            // Khong lay diem_sudung truoc, vi cot nay co the da la diem quy doi va se gay quy doi hai lan.
            BigDecimal diem = ct.getDiemGoc();
            if (diem == null) diem = ct.getDiemSudung();
            if (diem == null) diem = ct.getDiemQuydoi();
            return diem;
        }
        return null;
    }

    private boolean toHopCoMonAnh(ToHop toHop) {
        if (toHop == null || toHop.getTohopId() == null) return false;
        List<ToHopMon> ds = toHopDao.findMonByToHopId(toHop.getTohopId());
        for (ToHopMon thm : ds) {
            if (thm.getMon() != null && laMonAnh(thm.getMon())) {
                return true;
            }
        }
        return false;
    }

    private boolean monDatGiaiThuocToHop(ThiSinhThanhTich tt, ToHop toHop) {
        if (tt == null || toHop == null || isBlank(tt.getMonDatGiai())) {
            return false;
        }

        String monDatGiai = normalize(tt.getMonDatGiai());
        if (containsAny(monDatGiai, "KHAC")) {
            return false;
        }

        List<ToHopMon> ds = toHopDao.findMonByToHopId(toHop.getTohopId());
        for (ToHopMon thm : ds) {
            if (thm.getMon() == null) continue;

            String maMon = normalize(thm.getMon().getMaMon());
            String tenMon = normalize(thm.getMon().getTenMon());

            if (monDatGiai.equals(maMon) || monDatGiai.equals(tenMon)) return true;

            if ((containsAny(monDatGiai, "TOAN") || monDatGiai.equals("TO")) && maMon.equals("TO")) return true;
            if ((containsAny(monDatGiai, "VATLI", "VATLY") || monDatGiai.equals("LI")) && maMon.equals("LI")) return true;
            if ((containsAny(monDatGiai, "HOAHOC") || monDatGiai.equals("HO")) && maMon.equals("HO")) return true;
            if ((containsAny(monDatGiai, "SINHHOC") || monDatGiai.equals("SI")) && maMon.equals("SI")) return true;
            if ((containsAny(monDatGiai, "NGUVAN", "VAN") || monDatGiai.equals("VA")) && maMon.equals("VA")) return true;
            if ((containsAny(monDatGiai, "LICHSU") || monDatGiai.equals("SU")) && maMon.equals("SU")) return true;
            if ((containsAny(monDatGiai, "DIALI", "DIALY") || monDatGiai.equals("DI")) && maMon.equals("DI")) return true;
            if ((containsAny(monDatGiai, "TIENGANH", "NGOAINGU") || monDatGiai.equals("N1")) && maMon.equals("N1")) return true;
            if ((containsAny(monDatGiai, "GDKTPL", "KTPL")) && maMon.equals("KTPL")) return true;
        }

        return false;
    }

    private boolean isChungChiConHan(ThiSinhChungChi cc) {
        if (cc == null) return false;
        if (Boolean.FALSE.equals(cc.getIsHopLe())) return false;

        LocalDate mocSgu = LocalDate.of(2025, 6, 30);

        if (cc.getNgayHetHan() != null && cc.getNgayHetHan().isBefore(mocSgu)) {
            return false;
        }

        if (cc.getNgayCap() != null && cc.getNgayCap().isBefore(mocSgu.minusYears(2))) {
            return false;
        }

        return true;
    }

    private boolean isPhuongThucDGNLHCM(PhuongThuc pt) {
        if (pt == null) return false;
        String ma = normalize(pt.getMaPhuongthuc());
        String ten = normalize(pt.getTenPhuongthuc());
        return containsAny(ma, "PT2", "DGNL") || containsAny(ten, "DANHGIANANGLUC", "DGHCM", "DGNL");
    }

    private boolean isPhuongThucVSATHoacTHPT(PhuongThuc pt) {
        if (pt == null) return false;
        String ma = normalize(pt.getMaPhuongthuc());
        String ten = normalize(pt.getTenPhuongthuc());
        return containsAny(ma, "PT3", "PT4", "VSAT", "THPT")
                || containsAny(ten, "VSAT", "THPT", "THITOTNGHIEP");
    }

    private boolean isPhuongThucTHPT(PhuongThuc pt) {
        if (pt == null) return false;
        if (pt.getPhuongthucId() != null && pt.getPhuongthucId() == 2) return true;
        String ma = normalize(pt.getMaPhuongthuc());
        String ten = normalize(pt.getTenPhuongthuc());
        return containsAny(ma, "THPT", "PT1") || containsAny(ten, "THPT", "TOTNGHIEP");
    }

    private boolean isPhuongThucVSAT(PhuongThuc pt) {
        if (pt == null) return false;
        if (pt.getPhuongthucId() != null && pt.getPhuongthucId() == 3) return true;
        String ma = normalize(pt.getMaPhuongthuc());
        String ten = normalize(pt.getTenPhuongthuc());
        return containsAny(ma, "VSAT", "PT3") || containsAny(ten, "VSAT");
    }

    private String labelPhuongThuc(PhuongThuc pt) {
        if (pt == null) return "?";
        if (isPhuongThucTHPT(pt)) return "THPT";
        if (isPhuongThucVSAT(pt)) return "VSAT";
        if (isPhuongThucDGNLHCM(pt)) return "DGNL";
        return !isBlank(pt.getMaPhuongthuc()) ? pt.getMaPhuongthuc() : String.valueOf(pt.getPhuongthucId());
    }

    private boolean laMonAnh(Mon mon) {
        if (mon == null) return false;
        String ma = normalize(mon.getMaMon());
        String ten = normalize(mon.getTenMon());
        return "N1".equals(ma) || containsAny(ten, "TIENGANH", "ANH");
    }

    private BigDecimal parseBigDecimalMeta(String ghiChu, String key) {
        if (isBlank(ghiChu) || isBlank(key)) return null;

        Pattern pattern = Pattern.compile("(^|;)\\s*" + Pattern.quote(key) + "\\s*=\\s*([^;]+)");
        Matcher matcher = pattern.matcher(ghiChu);
        if (!matcher.find()) return null;

        String raw = matcher.group(2);
        if (raw == null) return null;

        raw = raw.trim().replace(",", ".");
        if (raw.isEmpty()) return null;

        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean containsAny(String value, String... needles) {
        if (value == null) return false;
        for (String needle : needles) {
            if (needle != null && value.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String s) {
        if (s == null) return "";
        String value = s.trim().toUpperCase(Locale.ROOT);

        value = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        value = value
                .replace("Đ", "D")
                .replaceAll("[\\s_\\-./:()]+", "");

        return value;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : ZERO;
    }

    /**
     * Tinh diem xet tuyen cho 1 nguyen vong.
     * Neu chua co DiemCong thi tu tao truoc.
     */
    public BigDecimal tinhDiemXettuyen(NguyenVong nv) {
        KetQuaDiem kq = tinhDiemTotNhat(nv);
        return kq != null ? kq.diemXettuyen : null;
    }

    /**
     * Chuan hoa diem to hop truoc khi luu DB.
     * - Lam tron 3 chu so thap phan theo yeu cau.
     * - Khong cho am do tru do lech khi khong co diem/thieu mon.
     * - Khong cho vuot thang 30.
     */
    private BigDecimal chuanHoaDiemToHop(BigDecimal diem) {
        if (diem == null) return ZERO.setScale(SCALE, ROUND);

        BigDecimal max = THIRTY.setScale(SCALE, ROUND);
        BigDecimal min = ZERO.setScale(SCALE, ROUND);
        BigDecimal value = diem.setScale(SCALE, ROUND);

        if (value.compareTo(max) > 0) return max;
        if (value.compareTo(min) < 0) return min;
        return value;
    }

    private BigDecimal capDiemXetTuyen(BigDecimal diem) {
        if (diem == null) return ZERO.setScale(SCALE, ROUND);

        BigDecimal max = THIRTY.setScale(SCALE, ROUND);
        BigDecimal min = ZERO.setScale(SCALE, ROUND);
        BigDecimal value = diem.setScale(SCALE, ROUND);

        if (value.compareTo(max) > 0) return max;
        if (value.compareTo(min) < 0) return min;
        return value;
    }

}