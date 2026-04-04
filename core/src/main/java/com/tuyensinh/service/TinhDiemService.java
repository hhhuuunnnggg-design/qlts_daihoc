package com.tuyensinh.service;

import com.tuyensinh.dao.*;
import com.tuyensinh.entity.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Engine tinh diem xet tuyen.
 *
 * Cong thuc tong quat:
 * diemThxt = sum(diem_sudung_i * heSo_i) - doLech
 * tongDiemCong = tongDiemChungChi + tongDiemUutienXt + tongDiemUutienQuyChe
 * diemXetTuyen = diemThxt + diemCong
 */
public class TinhDiemService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int SCALE = 5;
    private static final RoundingMode ROUND = RoundingMode.HALF_UP;

    private final BangQuyDoiDao bangQuyDoiDao = new BangQuyDoiDao();
    private final DiemThiDao diemThiDao = new DiemThiDao();
    private final DiemCongDao diemCongDao = new DiemCongDao();
    private final ToHopDao toHopDao = new ToHopDao();
    private final PhuongThucDao phuongThucDao = new PhuongThucDao();

    private final DiemCongService diemCongService = new DiemCongService();
    private final DiemCongChiTietService diemCongChiTietService = new DiemCongChiTietService();
    private final ThiSinhChungChiService thiSinhChungChiService = new ThiSinhChungChiService();
    private final ThiSinhThanhTichService thiSinhThanhTichService = new ThiSinhThanhTichService();

    /**
     * Ket qua tinh diem cho mot nguyen vong.
     */
    public static class KetQuaDiem {
        public BigDecimal diemThxt;       // tong hop (chua cong diem)
        public BigDecimal diemCong;       // tong diem cong
        public BigDecimal diemUutien;     // tong diem uu tien (XT + quy che)
        public BigDecimal diemXettuyen;   // diem cuoi cung
        public boolean coBangQuyDoi;      // da su dung bang quy doi
        public String ghiChu;

        @Override
        public String toString() {
            return String.format(
                    "THXT=%.2f | Cong=%.2f (UT=%.2f) | XetTuyen=%.2f | BQD=%s",
                    diemThxt, diemCong, diemUutien, diemXettuyen, coBangQuyDoi);
        }
    }

    /**
     * Tinh diem xet tuyen cho mot nguyen vong.
     */
    public KetQuaDiem tinhDiem(NguyenVong nv, DiemThi diemThi, Optional<DiemCong> diemCongOpt) {
        KetQuaDiem kq = new KetQuaDiem();
        kq.diemThxt = ZERO;
        kq.diemCong = ZERO;
        kq.diemUutien = ZERO;
        kq.coBangQuyDoi = false;

        if (diemThi == null || diemThi.getDanhSachDiemChiTiet() == null
                || diemThi.getDanhSachDiemChiTiet().isEmpty()) {
            kq.diemXettuyen = ZERO;
            kq.ghiChu = "Khong co du lieu diem thi";
            return kq;
        }

        NganhToHop nth = nv.getNganhToHop();
        if (nth == null) {
            kq.diemXettuyen = ZERO;
            kq.ghiChu = "Khong co thong tin nganh-to-hop";
            return kq;
        }

        ToHop toHop = nth.getToHop();
        if (toHop == null) {
            kq.diemXettuyen = ZERO;
            kq.ghiChu = "Khong co thong tin to hop";
            return kq;
        }

        PhuongThuc pt = nv.getPhuongThuc();

        // Lay danh sach mon cua to hop
        List<ToHopMon> monToHop = toHopDao.findMonByToHopId(toHop.getTohopId());
        Set<Integer> monToHopIds = new HashSet<>();
        for (ToHopMon thm : monToHop) {
            if (thm.getMon() != null) monToHopIds.add(thm.getMon().getMonId());
        }

        // He so / mon chinh tu NganhToHopMon (override)
        Map<Integer, BigDecimal> heSoMap = new HashMap<>();
        if (nth.getDanhSachNganhToHopMon() != null) {
            for (NganhToHopMon nthm : nth.getDanhSachNganhToHopMon()) {
                if (nthm.getMon() != null) {
                    heSoMap.put(nthm.getMon().getMonId(), new BigDecimal(nthm.getHeSo()));
                }
            }
        }

        BigDecimal tongDiem = ZERO;
        for (DiemThiChiTiet ct : diemThi.getDanhSachDiemChiTiet()) {
            if (ct.getMon() == null || !monToHopIds.contains(ct.getMon().getMonId())) {
                continue;
            }

            BigDecimal diemDung = ct.getDiemSudung();
            if (diemDung == null) diemDung = ct.getDiemGoc() != null ? ct.getDiemGoc() : ZERO;

            BigDecimal diemSauQD = timDiemQuyDoi(diemDung, pt, toHop, ct.getMon());
            if (diemSauQD != null) {
                diemDung = diemSauQD;
                kq.coBangQuyDoi = true;
            }

            BigDecimal heSo = heSoMap.get(ct.getMon().getMonId());
            if (heSo == null) heSo = BigDecimal.ONE;

            tongDiem = tongDiem.add(diemDung.multiply(heSo).setScale(SCALE, ROUND));
        }

        kq.diemThxt = tongDiem.setScale(SCALE, ROUND);

        BigDecimal doLech = nth.getDoLech();
        if (doLech != null && doLech.compareTo(ZERO) > 0) {
            kq.diemThxt = kq.diemThxt.subtract(doLech).setScale(SCALE, ROUND);
        }

        if (diemCongOpt != null && diemCongOpt.isPresent()) {
            DiemCong dc = diemCongOpt.get();
            kq.diemCong = safe(dc.getTongDiemCong());

            BigDecimal utXt = safe(dc.getTongDiemUutienXt());
            BigDecimal utQc = safe(dc.getTongDiemUutienQuyChe());
            kq.diemUutien = utXt.add(utQc);
        }

        kq.diemXettuyen = kq.diemThxt.add(kq.diemCong).setScale(SCALE, ROUND);
        kq.ghiChu = null;
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
     * Tao / cap nhat diem cong tu dong cho mot thi sinh theo cac nguyen vong da dang ky.
     * Nguon du lieu:
     * - Uu tien quy che: Khu vuc + Doi tuong tren ThiSinh
     * - Chung chi: xt_thisinh_chungchi
     * - Thanh tich: xt_thisinh_thanh_tich
     */
    public List<DiemCong> taoDiemCongTuDong(ThiSinh ts, PhuongThuc pt) {
        List<DiemCong> list = new ArrayList<>();

        List<NguyenVong> nvs = new NguyenVongDao().findByThiSinhId(ts.getThisinhId());
        Set<Integer> daCo = new HashSet<>();
        for (NguyenVong nv : nvs) {
            if (nv.getNganhToHop() != null && nv.getPhuongThuc() != null) {
                int key = nv.getNganhToHop().getNganhTohopId() * 1000
                        + (nv.getPhuongThuc().getPhuongthucId() != null
                        ? nv.getPhuongThuc().getPhuongthucId() : 0);
                daCo.add(key);
            }
        }

        for (Integer key : daCo) {
            int nthId = key / 1000;
            short ptId = (short) (key % 1000);
            if (ptId == 0 && pt != null) ptId = pt.getPhuongthucId();

            NganhToHop nth = new NganhToHopDao().findById(nthId);
            PhuongThuc ptReal = ptId > 0 ? phuongThucDao.findById((int) ptId) : pt;
            if (nth == null || ptReal == null) continue;

            Optional<DiemCong> opt = diemCongDao.findByThiSinhNganhToHopPhuongThuc(
                    ts.getThisinhId(), nthId, ptReal.getPhuongthucId());

            DiemCong dc;
            if (opt.isPresent()) {
                dc = opt.get();
                dc.setTongDiemChungChi(ZERO);
                dc.setTongDiemUutienXt(ZERO);
                dc.setTongDiemUutienQuyChe(ZERO);
                dc.setTongDiemCong(ZERO);
                diemCongService.update(dc);
                diemCongChiTietService.deleteByDiemCongId(dc.getDiemcongId());
            } else {
                dc = new DiemCong();
                dc.setThiSinh(ts);
                dc.setNganhToHop(nth);
                dc.setPhuongThuc(ptReal);
                dc.setTongDiemChungChi(ZERO);
                dc.setTongDiemUutienXt(ZERO);
                dc.setTongDiemUutienQuyChe(ZERO);
                dc.setTongDiemCong(ZERO);
                dc = diemCongService.save(dc);
            }

            List<DiemCongChiTiet> chiTietList = taoChiTietTuNguonGoc(ts, nth, ptReal, dc);
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
        if (ts.getKhuVucUutien() != null && ts.getKhuVucUutien().getMucDiem() != null
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
        if (ts.getDoiTuongUutien() != null && ts.getDoiTuongUutien().getMucDiem() != null
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

        // 3. Chung chi ngoai ngu theo SGU
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
                    DiemCongChiTiet ct = new DiemCongChiTiet();
                    ct.setDiemCong(dc);
                    ct.setLoaiNguon(DiemCongChiTiet.LoaiNguon.CC_NGOAI_NGU);
                    ct.setMaNguon(cc.getLoaiChungChi());
                    ct.setTenNguon(cc.getTenChungChi() != null ? cc.getTenChungChi() : cc.getLoaiChungChi());
                    ct.setCapApDung(pt != null ? pt.getMaPhuongthuc() : null);
                    ct.setMonLienQuan("N1");
                    ct.setGiaTriGoc(cc.getDiemGoc() != null ? cc.getDiemGoc().toPlainString() : null);
                    ct.setDiemQuyDoi(ZERO);
                    ct.setDiemCongGiaTri(diemCong);
                    ct.setThuTuUuTien(thuTu++);
                    ct.setIsApDung(true);
                    ct.setGhiChu("PT2 - cong diem khuyen khich tu chung chi");
                    list.add(ct);
                }
                continue;
            }

            if (laPT3or4) {
                if (toHopCoAnh) {
                    if (diemQdAnh.compareTo(ZERO) > 0) {
                        DiemCongChiTiet ct = new DiemCongChiTiet();
                        ct.setDiemCong(dc);
                        ct.setLoaiNguon(DiemCongChiTiet.LoaiNguon.CC_NGOAI_NGU);
                        ct.setMaNguon(cc.getLoaiChungChi());
                        ct.setTenNguon(cc.getTenChungChi() != null ? cc.getTenChungChi() : cc.getLoaiChungChi());
                        ct.setCapApDung(pt != null ? pt.getMaPhuongthuc() : null);
                        ct.setMonLienQuan("N1");
                        ct.setGiaTriGoc(cc.getDiemGoc() != null ? cc.getDiemGoc().toPlainString() : null);
                        ct.setDiemQuyDoi(diemQdAnh);
                        ct.setDiemCongGiaTri(ZERO);
                        ct.setThuTuUuTien(thuTu++);
                        ct.setIsApDung(true);
                        ct.setGhiChu("PT3/PT4 - quy doi diem mon Anh tu chung chi");
                        list.add(ct);
                    }
                } else {
                    if (diemCong.compareTo(ZERO) > 0) {
                        DiemCongChiTiet ct = new DiemCongChiTiet();
                        ct.setDiemCong(dc);
                        ct.setLoaiNguon(DiemCongChiTiet.LoaiNguon.CC_NGOAI_NGU);
                        ct.setMaNguon(cc.getLoaiChungChi());
                        ct.setTenNguon(cc.getTenChungChi() != null ? cc.getTenChungChi() : cc.getLoaiChungChi());
                        ct.setCapApDung(pt != null ? pt.getMaPhuongthuc() : null);
                        ct.setMonLienQuan("N1");
                        ct.setGiaTriGoc(cc.getDiemGoc() != null ? cc.getDiemGoc().toPlainString() : null);
                        ct.setDiemQuyDoi(ZERO);
                        ct.setDiemCongGiaTri(diemCong);
                        ct.setThuTuUuTien(thuTu++);
                        ct.setIsApDung(true);
                        ct.setGhiChu("PT3/PT4 - to hop khong co mon Anh, cong diem khuyen khich");
                        list.add(ct);
                    }
                }
            }
        }

        // 4. Thanh tich uu tien xet tuyen theo SGU
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

    private DiemCongChiTiet.LoaiNguon xacDinhLoaiNguonThanhTich(ThiSinhThanhTich tt) {
        String nhom = normalize(tt.getNhomThanhTich());
        String cap = normalize(tt.getCapThanhTich());
        String linhVuc = normalize(tt.getLinhVuc());

        if (nhom.contains("HSG") || nhom.contains("HOCSINHGIOI")) {
            if (cap.contains("QUOCGIA")) return DiemCongChiTiet.LoaiNguon.UTXT_HSG_QUOCGIA;
            if (cap.contains("TINH") || cap.contains("THANHPHO")) return DiemCongChiTiet.LoaiNguon.UTXT_HSG_TINH;
        }

        if (nhom.contains("KHKT") || linhVuc.contains("KHKT")) {
            return DiemCongChiTiet.LoaiNguon.UTXT_KHKT;
        }

        if (nhom.contains("NGHETHUAT") || nhom.contains("THETHAO")
                || linhVuc.contains("NGHETHUAT") || linhVuc.contains("THETHAO")) {
            return DiemCongChiTiet.LoaiNguon.UTXT_NGHE_THUAT;
        }

        return null;
    }

    private BigDecimal tinhDiemQuyDoiMonAnhSGU(ThiSinhChungChi cc) {
        if (cc == null || cc.getDiemGoc() == null) return ZERO;

        String loai = normalize(cc.getLoaiChungChi());
        BigDecimal diem = cc.getDiemGoc();

        if (loai.contains("IELTS")) {
            if (diem.compareTo(new BigDecimal("7.0")) >= 0) return new BigDecimal("10.0");
            if (diem.compareTo(new BigDecimal("5.5")) >= 0) return new BigDecimal("9.0");
            if (diem.compareTo(new BigDecimal("4.0")) >= 0) return new BigDecimal("8.0");
        }

        // Co the bo sung TOEFL/TOEIC sau khi doi chieu dung sheet Excel cua SGU
        return ZERO;
    }

    private BigDecimal tinhDiemCongKhuyenKhichChungChiSGU(ThiSinhChungChi cc) {
        if (cc == null || cc.getDiemGoc() == null) return ZERO;

        String loai = normalize(cc.getLoaiChungChi());
        BigDecimal diem = cc.getDiemGoc();

        if (loai.contains("IELTS")) {
            if (diem.compareTo(new BigDecimal("7.0")) >= 0) return new BigDecimal("2.0");
            if (diem.compareTo(new BigDecimal("5.5")) >= 0) return new BigDecimal("1.5");
            if (diem.compareTo(new BigDecimal("4.0")) >= 0) return new BigDecimal("1.0");
        }

        // Co the bo sung TOEFL/TOEIC sau khi doi chieu dung sheet Excel cua SGU
        return ZERO;
    }

    private BigDecimal tinhDiemCongThanhTichSGU(ThiSinhThanhTich tt, boolean monTrungToHop) {
        if (tt == null) return ZERO;

        String nhom = normalize(tt.getNhomThanhTich());
        String cap = normalize(tt.getCapThanhTich());
        String loai = normalize(tt.getLoaiGiai());
        String linhVuc = normalize(tt.getLinhVuc());

        // HSG quoc gia
        if ((nhom.contains("HSG") || nhom.contains("HOCSINHGIOI")) && cap.contains("QUOCGIA")) {
            if (loai.contains("NHI")) return monTrungToHop ? new BigDecimal("2.0") : new BigDecimal("0.75");
            if (loai.contains("BA")) return monTrungToHop ? new BigDecimal("1.5") : new BigDecimal("0.50");
            if (loai.contains("KHUYENKHICH")) return monTrungToHop ? new BigDecimal("1.0") : ZERO;
        }

        // HSG tinh / thanh pho
        if ((nhom.contains("HSG") || nhom.contains("HOCSINHGIOI"))
                && (cap.contains("TINH") || cap.contains("THANHPHO"))) {
            if (loai.contains("NHAT")) return monTrungToHop ? new BigDecimal("1.0") : new BigDecimal("0.25");
            if (loai.contains("NHI")) return monTrungToHop ? new BigDecimal("0.75") : ZERO;
            if (loai.contains("BA")) return monTrungToHop ? new BigDecimal("0.50") : ZERO;
        }

        // KHKT quoc gia - toi uu theo thong bao SGU: giai Tu duoc uu tien xet tuyen
        if (nhom.contains("KHKT") || linhVuc.contains("KHKT")) {
            if (cap.contains("QUOCGIA")) {
                if (loai.contains("NHAT")) return monTrungToHop ? new BigDecimal("2.0") : new BigDecimal("0.75");
                if (loai.contains("NHI")) return monTrungToHop ? new BigDecimal("1.5") : new BigDecimal("0.50");
                if (loai.contains("BA")) return monTrungToHop ? new BigDecimal("1.0") : ZERO;
                if (loai.contains("TU")) return monTrungToHop ? new BigDecimal("0.5") : ZERO;
            }
        }

        // Nghe thuat / the thao: tam thoi gom ve 1 muc co kiem soat
        if (nhom.contains("NGHETHUAT") || nhom.contains("THETHAO")
                || linhVuc.contains("NGHETHUAT") || linhVuc.contains("THETHAO")) {
            if (cap.contains("QUOCGIA")) return new BigDecimal("1.0");
            if (cap.contains("TINH") || cap.contains("THANHPHO")) return new BigDecimal("0.5");
        }

        return ZERO;
    }

    private boolean toHopCoMonAnh(ToHop toHop) {
        if (toHop == null || toHop.getTohopId() == null) return false;
        List<ToHopMon> ds = toHopDao.findMonByToHopId(toHop.getTohopId());
        for (ToHopMon thm : ds) {
            if (thm.getMon() == null || thm.getMon().getMaMon() == null) continue;
            if ("N1".equalsIgnoreCase(thm.getMon().getMaMon())) return true;
        }
        return false;
    }

    private boolean monDatGiaiThuocToHop(ThiSinhThanhTich tt, ToHop toHop) {
        if (tt == null || toHop == null || tt.getMonDatGiai() == null || tt.getMonDatGiai().isBlank()) {
            return false;
        }

        String monDatGiai = normalize(tt.getMonDatGiai());
        List<ToHopMon> ds = toHopDao.findMonByToHopId(toHop.getTohopId());
        for (ToHopMon thm : ds) {
            if (thm.getMon() == null) continue;
            String maMon = normalize(thm.getMon().getMaMon());
            String tenMon = normalize(thm.getMon().getTenMon());

            if (monDatGiai.equals(maMon) || monDatGiai.equals(tenMon)) return true;

            // mapping tay cho cac mon hay nhap khac nhau
            if ((monDatGiai.contains("TOAN") || monDatGiai.equals("TO")) && maMon.equals("TO")) return true;
            if ((monDatGiai.contains("VATLI") || monDatGiai.contains("VATLY") || monDatGiai.equals("LI")) && maMon.equals("LI")) return true;
            if ((monDatGiai.contains("HOAHOC") || monDatGiai.equals("HO")) && maMon.equals("HO")) return true;
            if ((monDatGiai.contains("SINHHOC") || monDatGiai.equals("SI")) && maMon.equals("SI")) return true;
            if ((monDatGiai.contains("NGUVAN") || monDatGiai.contains("VAN") || monDatGiai.equals("VA")) && maMon.equals("VA")) return true;
            if ((monDatGiai.contains("LICHSU") || monDatGiai.equals("SU")) && maMon.equals("SU")) return true;
            if ((monDatGiai.contains("DIALI") || monDatGiai.equals("DI")) && maMon.equals("DI")) return true;
            if ((monDatGiai.contains("TIENGANH") || monDatGiai.contains("NGOAINGU") || monDatGiai.equals("N1")) && maMon.equals("N1")) return true;
            if ((monDatGiai.contains("GDKTPL") || monDatGiai.contains("KTPL")) && maMon.equals("KTPL")) return true;
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
        return ma.equals("PT2") || ten.contains("DANHGIANANGLUC") || ten.contains("DGHCM");
    }

    private boolean isPhuongThucVSATHoacTHPT(PhuongThuc pt) {
        if (pt == null) return false;
        String ma = normalize(pt.getMaPhuongthuc());
        String ten = normalize(pt.getTenPhuongthuc());
        return ma.equals("PT3") || ma.equals("PT4")
                || ten.contains("VSAT")
                || ten.contains("THPT")
                || ten.contains("THI TOT NGHIEP");
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim()
                .toUpperCase(Locale.ROOT)
                .replace(" ", "")
                .replace("_", "");
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : ZERO;
    }

    /**
     * Tinh diem cho mot nguyen vong — tra ve diem xet tuyen cuoi cung.
     */
    public BigDecimal tinhDiemXettuyen(NguyenVong nv) {
        if (nv.getThiSinh() == null || nv.getPhuongThuc() == null) return null;

        DiemThi diemThi = diemThiDao.findByThiSinhAndPhuongThuc(
                nv.getThiSinh().getThisinhId(),
                nv.getPhuongThuc().getPhuongthucId(),
                (short) 2026
        ).orElse(null);

        Optional<DiemCong> diemCong = Optional.empty();
        if (nv.getNganhToHop() != null) {
            diemCong = diemCongDao.findByThiSinhNganhToHopPhuongThuc(
                    nv.getThiSinh().getThisinhId(),
                    nv.getNganhToHop().getNganhTohopId(),
                    nv.getPhuongThuc().getPhuongthucId()
            );
        }

        KetQuaDiem kq = tinhDiem(nv, diemThi, diemCong);
        return kq.diemXettuyen;
    }
}