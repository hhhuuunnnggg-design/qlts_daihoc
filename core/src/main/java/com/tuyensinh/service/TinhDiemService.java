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
 * - Diem THXT: tong diem cac mon trong to hop, co tinh he so va do lech
 * - Diem quy doi chung chi Anh: doc tu xt_thisinh_chungchi.ghi_chu (excel_diem_quy_doi)
 * - Diem cong chung chi / thanh tich: doc tu ghi_chu cua bang nguon import
 * - Diem cong tong = chung chi + uu tien xet tuyen + uu tien quy che
 */
public class TinhDiemService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final int SCALE = 5;
    private static final RoundingMode ROUND = RoundingMode.HALF_UP;
    private static final short NAM_TUYEN_SINH_MAC_DINH = 2026;

    private final BangQuyDoiDao bangQuyDoiDao = new BangQuyDoiDao();
    private final DiemThiDao diemThiDao = new DiemThiDao();
    private final DiemCongDao diemCongDao = new DiemCongDao();
    private final ToHopDao toHopDao = new ToHopDao();
    private final PhuongThucDao phuongThucDao = new PhuongThucDao();
    private final NguyenVongDao nguyenVongDao = new NguyenVongDao();
    private final NganhToHopDao nganhToHopDao = new NganhToHopDao();

    private final DiemCongService diemCongService = new DiemCongService();
    private final DiemCongChiTietService diemCongChiTietService = new DiemCongChiTietService();
    private final ThiSinhChungChiService thiSinhChungChiService = new ThiSinhChungChiService();
    private final ThiSinhThanhTichService thiSinhThanhTichService = new ThiSinhThanhTichService();

    public static class KetQuaDiem {
        public BigDecimal diemThxt;
        public BigDecimal diemCong;
        public BigDecimal diemUutien;
        public BigDecimal diemXettuyen;
        public boolean coBangQuyDoi;
        public String ghiChu;

        @Override
        public String toString() {
            return String.format(
                    "THXT=%.2f | Cong=%.2f (UT=%.2f) | XetTuyen=%.2f | BQD=%s | GhiChu=%s",
                    safeForLog(diemThxt),
                    safeForLog(diemCong),
                    safeForLog(diemUutien),
                    safeForLog(diemXettuyen),
                    coBangQuyDoi,
                    ghiChu
            );
        }

        private static BigDecimal safeForLog(BigDecimal value) {
            return value != null ? value : BigDecimal.ZERO;
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

        Set<Integer> monToHopIds = new HashSet<>();
        Integer monAnhId = null;
        for (ToHopMon thm : monToHop) {
            if (thm.getMon() == null || thm.getMon().getMonId() == null) continue;
            monToHopIds.add(thm.getMon().getMonId());
            if (laMonAnh(thm.getMon())) {
                monAnhId = thm.getMon().getMonId();
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

        BigDecimal tongDiem = ZERO;
        List<String> notes = new ArrayList<>();

        for (ToHopMon thm : monToHop) {
            if (thm.getMon() == null || thm.getMon().getMonId() == null) continue;

            Mon mon = thm.getMon();
            Integer monId = mon.getMonId();
            BigDecimal heSo = heSoMap.getOrDefault(monId, ONE);
            BigDecimal diemDung = null;

            // Neu mon la Anh va co quy doi tu chung chi -> uu tien dung diem quy doi nay
            if (laMonAnh(mon) && diemQuyDoiTiengAnhTuCc.compareTo(ZERO) > 0) {
                diemDung = diemQuyDoiTiengAnhTuCc;
                kq.coBangQuyDoi = true;
                notes.add("Dung diem quy doi chung chi cho mon " + safe(mon.getMaMon()));
            } else {
                DiemThiChiTiet ct = diemTheoMon.get(monId);
                if (ct != null) {
                    diemDung = ct.getDiemSudung();
                    if (diemDung == null) {
                        diemDung = ct.getDiemQuydoi();
                    }
                    if (diemDung == null) {
                        diemDung = ct.getDiemGoc();
                    }

                    if (diemDung != null) {
                        BigDecimal diemSauBangQd = timDiemQuyDoi(diemDung, pt, toHop, mon);
                        if (diemSauBangQd != null) {
                            diemDung = diemSauBangQd;
                            kq.coBangQuyDoi = true;
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

        kq.diemThxt = tongDiem.setScale(SCALE, ROUND);

        BigDecimal doLech = nth.getDoLech();
        if (doLech != null && doLech.compareTo(ZERO) > 0) {
            kq.diemThxt = kq.diemThxt.subtract(doLech).setScale(SCALE, ROUND);
            notes.add("Tru do lech " + doLech.toPlainString());
        }

        if (diemCongOpt != null && diemCongOpt.isPresent()) {
            DiemCong dc = diemCongOpt.get();
            kq.diemCong = safe(dc.getTongDiemCong());

            BigDecimal utXt = safe(dc.getTongDiemUutienXt());
            BigDecimal utQc = safe(dc.getTongDiemUutienQuyChe());
            kq.diemUutien = utXt.add(utQc);
        }

        kq.diemXettuyen = kq.diemThxt.add(kq.diemCong).setScale(SCALE, ROUND);
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
     * Tao / cap nhat diem cong tu dong cho mot thi sinh theo cac nguyen vong.
     */
    public List<DiemCong> taoDiemCongTuDong(ThiSinh ts, PhuongThuc ptMacDinh) {
        List<DiemCong> list = new ArrayList<>();
        if (ts == null || ts.getThisinhId() == null) return list;

        List<NguyenVong> nvs = nguyenVongDao.findByThiSinhId(ts.getThisinhId());
        Set<String> uniquePairs = new LinkedHashSet<>();

        for (NguyenVong nv : nvs) {
            if (nv.getNganhToHop() == null) continue;

            Short ptId = nv.getPhuongThuc() != null ? nv.getPhuongThuc().getPhuongthucId() : null;
            if (ptId == null && ptMacDinh != null) {
                ptId = ptMacDinh.getPhuongthucId();
            }
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
            for (DiemCongChiTiet ct : chiTietList) {
                diemCongChiTietService.save(ct);
            }

            diemCongService.recalculateTongHop(dc.getDiemcongId());
            list.add(diemCongDao.findById(dc.getDiemcongId()));
        }

        return list;
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
        if (nv == null || nv.getThiSinh() == null || nv.getPhuongThuc() == null) {
            return null;
        }

        DiemThi diemThi = diemThiDao.findByThiSinhAndPhuongThuc(
                nv.getThiSinh().getThisinhId(),
                nv.getPhuongThuc().getPhuongthucId(),
                NAM_TUYEN_SINH_MAC_DINH
        ).orElse(null);

        Optional<DiemCong> diemCong = Optional.empty();
        if (nv.getNganhToHop() != null) {
            diemCong = diemCongDao.findByThiSinhNganhToHopPhuongThuc(
                    nv.getThiSinh().getThisinhId(),
                    nv.getNganhToHop().getNganhTohopId(),
                    nv.getPhuongThuc().getPhuongthucId()
            );

            if (!diemCong.isPresent()) {
                taoDiemCongTuDong(nv.getThiSinh(), nv.getPhuongThuc());

                diemCong = diemCongDao.findByThiSinhNganhToHopPhuongThuc(
                        nv.getThiSinh().getThisinhId(),
                        nv.getNganhToHop().getNganhTohopId(),
                        nv.getPhuongThuc().getPhuongthucId()
                );
            }
        }

        KetQuaDiem kq = tinhDiem(nv, diemThi, diemCong);
        return kq.diemXettuyen;
    }
}