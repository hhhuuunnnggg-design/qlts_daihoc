package com.tuyensinh.service;

import com.tuyensinh.dao.*;
import com.tuyensinh.entity.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Engine xet tuyen.
 *
 * Diem quan trong cua ban nay:
 * - Tinh diem tot nhat cho tung nguyen vong: max(THPT, VSAT, DGNL sau quy doi).
 * - Xet tuyen toan cuc theo thu tu nguyen vong cua tung thi sinh.
 * - Moi thi sinh chi duoc TRUNG_TUYEN mot nguyen vong cao nhat.
 */
public class XetTuyenEngine {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final TinhDiemService tinhDiemService = new TinhDiemService();
    private final NguyenVongDao nguyenVongDao = new NguyenVongDao();
    private final NganhPhuongThucDao nganhPhuongThucDao = new NganhPhuongThucDao();
    private final NganhDao nganhDao = new NganhDao();
    private final PhuongThucDao phuongThucDao = new PhuongThucDao();

    /** Ket qua chi tiet cua mot nguyen vong sau khi xet tuyen. */
    public static class KetQuaXetTuyen {
        public final NguyenVong nguyenVong;
        public final TinhDiemService.KetQuaDiem ketQuaDiem;
        public final String ketQua;
        public final BigDecimal diemSan;
        public final Integer chiTieu;
        public final int thuTuHienTai;
        public String ghiChu;

        public KetQuaXetTuyen(NguyenVong nv, TinhDiemService.KetQuaDiem kqDiem,
                              String ketQua, BigDecimal diemSan, Integer chiTieu, int thuTu) {
            this.nguyenVong = nv;
            this.ketQuaDiem = kqDiem;
            this.ketQua = ketQua;
            this.diemSan = diemSan;
            this.chiTieu = chiTieu;
            this.thuTuHienTai = thuTu;
        }

        public String toString() {
            return String.format("[%s] %s | NV=%s | Nganh=%s | Nguon=%s | THXT=%.2f | Cong=%.2f | XT=%.2f | Hang=%d/%s",
                    ketQua,
                    nguyenVong.getThiSinh() != null ? nguyenVong.getThiSinh().getHoVaTen() : "?",
                    nguyenVong.getThuTu() != null ? nguyenVong.getThuTu() : 0,
                    nguyenVong.getNganh() != null ? nguyenVong.getNganh().getMaNganh() : "?",
                    ketQuaDiem != null && ketQuaDiem.phuongThucDiemTotNhat != null
                            ? ketQuaDiem.phuongThucDiemTotNhat
                            : (nguyenVong.getPhuongThucDiemTotNhat() != null ? nguyenVong.getPhuongThucDiemTotNhat() : "?"),
                    ketQuaDiem != null && ketQuaDiem.diemThxt != null ? ketQuaDiem.diemThxt : ZERO,
                    ketQuaDiem != null && ketQuaDiem.diemCong != null ? ketQuaDiem.diemCong : ZERO,
                    ketQuaDiem != null && ketQuaDiem.diemXettuyen != null ? ketQuaDiem.diemXettuyen : ZERO,
                    thuTuHienTai,
                    chiTieu != null && chiTieu < Integer.MAX_VALUE ? String.valueOf(chiTieu) : "khong gioi han");
        }
    }

    /** Ket qua tong hop cua mot dot xet tuyen theo nganh/phuong thuc. */
    public static class DotXetTuyenResult {
        public final Nganh nganh;
        public final PhuongThuc phuongThuc;
        public final List<KetQuaXetTuyen> danhSach;
        public int soTrungTuyen = 0;
        public int soTruot = 0;
        public int soChoXet = 0;
        public int soPhoiDuKien = 0;
        public int soThieuDiem = 0;

        public DotXetTuyenResult(Nganh n, PhuongThuc pt, List<KetQuaXetTuyen> ds) {
            this.nganh = n;
            this.phuongThuc = pt;
            this.danhSach = ds;
            for (KetQuaXetTuyen kq : ds) {
                String kq2 = kq.ketQua;
                if (NguyenVong.KetQua.TRUNG_TUYEN.equals(kq2)) soTrungTuyen++;
                else if (NguyenVong.KetQua.TRUOT.equals(kq2)) soTruot++;
                else if (NguyenVong.KetQua.CHO_XET.equals(kq2)) soChoXet++;
                else if (NguyenVong.KetQua.PHOI_DU_KIEN.equals(kq2)) soPhoiDuKien++;
                if (kq.ketQuaDiem != null && "Khong co du lieu diem thi".equals(kq.ketQuaDiem.ghiChu)) {
                    soThieuDiem++;
                }
            }
        }
    }

    /** Ket qua tong hop khi chay xet tuyen toan cuc theo thu tu nguyen vong. */
    public static class XetTuyenToanCucResult {
        public final List<KetQuaXetTuyen> danhSach;
        public int soNguyenVong = 0;
        public int soThiSinh = 0;
        public int soTrungTuyen = 0;
        public int soTruot = 0;
        public int soChoXet = 0;
        public int soPhoiDuKien = 0;
        public int soTinhDiemOk = 0;
        public int soTinhDiemLoi = 0;
        public int soVongLap = 0;
        public final Map<String, Integer> thongKeTheoNhom = new LinkedHashMap<>();

        public XetTuyenToanCucResult(List<KetQuaXetTuyen> danhSach) {
            this.danhSach = danhSach;
        }
    }

    /** Tinh diem cho mot nguyen vong va cap nhat vao DB. */
    public TinhDiemService.KetQuaDiem tinhDiemNguyenVong(NguyenVong nv) {
        if (nv == null || nv.getThiSinh() == null) return null;

        TinhDiemService.KetQuaDiem kq = tinhDiemService.tinhDiemTotNhat(nv);
        ganKetQuaDiemVaoNguyenVong(nv, kq);
        nguyenVongDao.update(nv);
        return kq;
    }

    /**
     * Xet tuyen rieng mot nganh/phuong thuc. Ham nay giu lai de test nhanh theo nganh,
     * nhung KHONG phai cach chay chinh thuc vi no khong biet nguyen vong cao hon cua thi sinh o nganh khac.
     */
    public DotXetTuyenResult xetTuyenNganhPhuongThuc(Integer nganhId, Short phuongthucId) {
        Nganh nganh = nganhDao.findById(nganhId);
        if (nganh == null) throw new IllegalArgumentException("Nganh not found: " + nganhId);

        PhuongThuc phuongThuc = phuongthucId != null ? phuongThucDao.findById(phuongthucId) : null;
        Integer chiTieu = layChiTieu(nganh, phuongThuc);
        BigDecimal diemSan = nganh.getDiemSan();

        List<NguyenVong> nvs = (phuongthucId != null)
                ? nguyenVongDao.findByNganhIdAndPhuongThuc(nganhId, phuongthucId)
                : nguyenVongDao.findByNganhId(nganhId);

        nvs.sort(this::compareTheoDiem);

        List<KetQuaXetTuyen> results = new ArrayList<>();
        int countTrungTuyen = 0;

        for (int i = 0; i < nvs.size(); i++) {
            NguyenVong nv = nvs.get(i);
            TinhDiemService.KetQuaDiem kqDiem = ketQuaTuNguyenVong(nv);
            String ketQua;

            if (!coDiemHopLe(nv)) {
                ketQua = NguyenVong.KetQua.CHO_XET;
            } else if (!datDiemSan(nv)) {
                ketQua = NguyenVong.KetQua.TRUOT;
            } else if (chiTieu <= 0 || countTrungTuyen < chiTieu) {
                ketQua = NguyenVong.KetQua.TRUNG_TUYEN;
                countTrungTuyen++;
            } else {
                ketQua = NguyenVong.KetQua.PHOI_DU_KIEN;
            }

            nv.setKetQua(ketQua);
            nguyenVongDao.update(nv);
            results.add(new KetQuaXetTuyen(nv, kqDiem, ketQua, diemSan, chiTieu, i + 1));
        }

        capNhatSoLuongHienTai(nganh, phuongThuc, countTrungTuyen);
        return new DotXetTuyenResult(nganh, phuongThuc, results);
    }

    /** Xet tuyen mau tren mot so nguyen vong dau tien cua nganh/phuong thuc, khong ghi ket_qua. */
    public List<KetQuaXetTuyen> xetTuyenMau(Integer nganhId, Short phuongthucId, int limit) {
        Nganh nganh = nganhDao.findById(nganhId);
        if (nganh == null) throw new IllegalArgumentException("Nganh not found: " + nganhId);

        PhuongThuc phuongThuc = phuongthucId != null ? phuongThucDao.findById(phuongthucId) : null;
        BigDecimal diemSan = nganh.getDiemSan();
        Integer chiTieu = layChiTieu(nganh, phuongThuc);

        List<NguyenVong> nvs = (phuongthucId != null)
                ? nguyenVongDao.findByNganhIdAndPhuongThuc(nganhId, phuongthucId)
                : nguyenVongDao.findByNganhId(nganhId);

        if (limit > 0 && nvs.size() > limit) nvs = new ArrayList<>(nvs.subList(0, limit));
        else nvs = new ArrayList<>(nvs);

        List<KetQuaXetTuyen> results = new ArrayList<>();
        for (NguyenVong nv : nvs) {
            TinhDiemService.KetQuaDiem kq = tinhDiemNguyenVong(nv);
            results.add(new KetQuaXetTuyen(nv, kq, NguyenVong.KetQua.CHO_XET, diemSan, chiTieu, 0));
        }

        results.sort((a, b) -> compareTheoDiem(a.nguyenVong, b.nguyenVong));

        List<KetQuaXetTuyen> simulated = new ArrayList<>();
        int countTrungTuyen = 0;
        int thuTu = 1;
        for (KetQuaXetTuyen item : results) {
            NguyenVong nv = item.nguyenVong;
            String ketQua;
            if (!coDiemHopLe(nv)) ketQua = NguyenVong.KetQua.CHO_XET;
            else if (!datDiemSan(nv)) ketQua = NguyenVong.KetQua.TRUOT;
            else if (chiTieu <= 0 || countTrungTuyen < chiTieu) {
                ketQua = NguyenVong.KetQua.TRUNG_TUYEN;
                countTrungTuyen++;
            } else ketQua = NguyenVong.KetQua.PHOI_DU_KIEN;

            simulated.add(new KetQuaXetTuyen(nv, item.ketQuaDiem, ketQua, diemSan, chiTieu, thuTu++));
        }
        return simulated;
    }

    /**
     * Xet tuyen toan cuc theo thu tu nguyen vong.
     *
     * Thuat toan mo phong nguyen tac:
     * - Thi sinh nop vao nguyen vong cao nhat con co the xet.
     * - Moi nganh/phuong thuc tam giu top theo diem trong chi tieu.
     * - Thi sinh bi loai se chuyen xuong nguyen vong tiep theo.
     * - Ket thuc khi khong con thi sinh nao co nguyen vong tiep theo de nop.
     */
    public XetTuyenToanCucResult xetTuyenToanCucTheoThuTuNguyenVong(
            Short phuongthucIdFilter,
            Integer nganhIdFilter,
            boolean tinhDiemTruoc) {

        /*
         * BAN NGHIEP VU: XET THEO CHI_TIEU NGANH
         *
         * Nguyen tac:
         * 1. Moi nguyen vong duoc tinh diem tot nhat: max(THPT, VSAT, DGNL sau quy doi).
         * 2. Chi xet nhung nguyen vong co diem hop le va dat diem_san cua nganh.
         * 3. Thi sinh duoc nop tu NV1 -> NV2 -> NV3...
         * 4. Moi nganh tam giu top thi sinh theo diem_xettuyen trong pham vi xt_nganh.chi_tieu.
         * 5. Thi sinh bi loai khoi nganh do het chi tieu se duoc chuyen xuong nguyen vong tiep theo.
         * 6. Moi thi sinh chi trung tuyen 1 nguyen vong cao nhat co the trung.
         * 7. diem_trung_tuyen cua xt_nganh = diem_xettuyen thap nhat trong danh sach trung tuyen cua nganh.
         */

        List<NguyenVong> ds = locNguyenVong(nguyenVongDao.findAllForXetTuyen(), phuongthucIdFilter, nganhIdFilter);
        XetTuyenToanCucResult result = new XetTuyenToanCucResult(new ArrayList<>());
        result.soNguyenVong = ds.size();

        if (tinhDiemTruoc) {
            for (NguyenVong nv : ds) {
                try {
                    TinhDiemService.KetQuaDiem kq = tinhDiemService.tinhDiemTotNhat(nv);
                    ganKetQuaDiemVaoNguyenVong(nv, kq);
                    result.soTinhDiemOk++;
                } catch (Exception e) {
                    result.soTinhDiemLoi++;
                    if (nv != null) {
                        nv.setDiemThxt(ZERO);
                        nv.setDiemCong(ZERO);
                        nv.setDiemUutien(ZERO);
                        nv.setDiemXettuyen(ZERO);
                        nv.setPhuongThucDiemTotNhat(null);
                        nv.setKetQua(NguyenVong.KetQua.TRUOT);
                        nv.setGhiChu(appendNote(nv.getGhiChu(), "LOI_TINH_DIEM: " + e.getMessage()));
                    }
                }
            }
        }

        Map<Integer, List<NguyenVong>> nvTheoThiSinh = new LinkedHashMap<>();
        Map<Integer, Nganh> nganhTrongDot = new LinkedHashMap<>();

        for (NguyenVong nv : ds) {
            if (nv == null) continue;

            if (nv.getNganh() != null && nv.getNganh().getNganhId() != null) {
                nganhTrongDot.putIfAbsent(nv.getNganh().getNganhId(), nv.getNganh());
            }

            if (nv.getThiSinh() == null || nv.getThiSinh().getThisinhId() == null) {
                continue;
            }

            nvTheoThiSinh
                    .computeIfAbsent(nv.getThiSinh().getThisinhId(), k -> new ArrayList<>())
                    .add(nv);
        }

        result.soThiSinh = nvTheoThiSinh.size();

        // Sap xep nguyen vong cua tung thi sinh theo thu tu NV tang dan.
        for (List<NguyenVong> list : nvTheoThiSinh.values()) {
            list.sort(Comparator
                    .comparing((NguyenVong nv) -> nv.getThuTu() != null ? nv.getThuTu() : Integer.MAX_VALUE)
                    .thenComparing(nv -> nv.getNguyenvongId() != null ? nv.getNguyenvongId() : Integer.MAX_VALUE));
        }

        /*
         * Deferred acceptance:
         * - dangXet: thi sinh dang can nop nguyen vong tiep theo.
         * - nextIndexTheoThiSinh: vi tri nguyen vong tiep theo se thu.
         * - giuTheoNganh: danh sach thi sinh dang duoc nganh tam giu.
         * - giuTheoThiSinh: nguyen vong dang duoc tam giu cua tung thi sinh.
         */
        Map<Integer, Integer> nextIndexTheoThiSinh = new LinkedHashMap<>();
        Set<Integer> dangXet = new LinkedHashSet<>(nvTheoThiSinh.keySet());
        Map<Integer, NguyenVong> giuTheoThiSinh = new LinkedHashMap<>();
        Map<Integer, List<NguyenVong>> giuTheoNganh = new LinkedHashMap<>();

        result.soVongLap = 0;
        int maxLoop = Math.max(1, ds.size() + 5);

        while (!dangXet.isEmpty() && result.soVongLap < maxLoop) {
            result.soVongLap++;

            Map<Integer, List<NguyenVong>> deXuatTheoNganh = new LinkedHashMap<>();

            for (Integer thisinhId : dangXet) {
                List<NguyenVong> prefs = nvTheoThiSinh.get(thisinhId);
                if (prefs == null || prefs.isEmpty()) continue;

                int idx = nextIndexTheoThiSinh.getOrDefault(thisinhId, 0);
                NguyenVong deXuat = null;

                while (idx < prefs.size()) {
                    NguyenVong nv = prefs.get(idx);
                    idx++;
                    nextIndexTheoThiSinh.put(thisinhId, idx);

                    // Khong de xuat nhung NV khong co diem, duoi san, hoac nganh khong co chi tieu.
                    // Cac NV nay se duoc set TRUOT va ghi chu o buoc tong ket cuoi ham.
                    if (coDiemHopLe(nv) && datDiemSan(nv) && layChiTieuNganh(nv) > 0) {
                        deXuat = nv;
                        break;
                    }
                }

                if (deXuat != null && deXuat.getNganh() != null && deXuat.getNganh().getNganhId() != null) {
                    Integer nganhId = deXuat.getNganh().getNganhId();
                    deXuatTheoNganh.computeIfAbsent(nganhId, k -> new ArrayList<>()).add(deXuat);
                }
            }

            if (deXuatTheoNganh.isEmpty()) {
                break;
            }

            Set<Integer> biLoaiVongNay = new LinkedHashSet<>();

            for (Map.Entry<Integer, List<NguyenVong>> entry : deXuatTheoNganh.entrySet()) {
                Integer nganhId = entry.getKey();
                List<NguyenVong> pool = new ArrayList<>();

                List<NguyenVong> dangGiu = giuTheoNganh.get(nganhId);
                if (dangGiu != null) pool.addAll(dangGiu);
                pool.addAll(entry.getValue());

                pool.sort(this::compareTheoDiem);

                int chiTieu = 0;
                if (!pool.isEmpty()) chiTieu = layChiTieuNganh(pool.get(0));

                List<NguyenVong> duocGiu = new ArrayList<>();
                List<NguyenVong> biLoai = new ArrayList<>();

                for (int i = 0; i < pool.size(); i++) {
                    NguyenVong nv = pool.get(i);
                    if (i < chiTieu) duocGiu.add(nv);
                    else biLoai.add(nv);
                }

                giuTheoNganh.put(nganhId, duocGiu);

                for (NguyenVong nv : duocGiu) {
                    Integer tsId = layThiSinhId(nv);
                    if (tsId != null) giuTheoThiSinh.put(tsId, nv);
                }

                for (NguyenVong nv : biLoai) {
                    Integer tsId = layThiSinhId(nv);
                    if (tsId != null) {
                        giuTheoThiSinh.remove(tsId);
                        biLoaiVongNay.add(tsId);
                    }
                }
            }

            dangXet = biLoaiVongNay;
        }

        // Tap trung tuyen cuoi cung.
        Set<Integer> idNguyenVongTrung = new HashSet<>();
        Map<Integer, NguyenVong> nvTrungTuyenTheoThiSinh = new LinkedHashMap<>();
        Map<Integer, List<NguyenVong>> trungTuyenTheoNganh = new LinkedHashMap<>();
        Map<String, List<NguyenVong>> trungTuyenTheoNhomPhuongThuc = new LinkedHashMap<>();

        for (NguyenVong nv : giuTheoThiSinh.values()) {
            if (nv == null || nv.getNguyenvongId() == null) continue;

            Integer tsId = layThiSinhId(nv);
            Integer nganhId = nv.getNganh() != null ? nv.getNganh().getNganhId() : null;

            idNguyenVongTrung.add(nv.getNguyenvongId());
            if (tsId != null) nvTrungTuyenTheoThiSinh.put(tsId, nv);
            if (nganhId != null) trungTuyenTheoNganh.computeIfAbsent(nganhId, k -> new ArrayList<>()).add(nv);
            trungTuyenTheoNhomPhuongThuc.computeIfAbsent(nhomXetTuyenKey(nv), k -> new ArrayList<>()).add(nv);
        }

        // Diem trung tuyen cua nganh = diem thap nhat trong danh sach trung tuyen nganh do.
        Map<Integer, BigDecimal> diemTrungTuyenTheoNganh = new LinkedHashMap<>();
        for (Integer nganhId : nganhTrongDot.keySet()) {
            diemTrungTuyenTheoNganh.put(nganhId, null);
        }

        for (Map.Entry<Integer, List<NguyenVong>> entry : trungTuyenTheoNganh.entrySet()) {
            List<NguyenVong> list = entry.getValue();
            list.sort(this::compareTheoDiem);
            BigDecimal diemChuan = null;
            if (!list.isEmpty()) {
                NguyenVong cuoi = list.get(list.size() - 1);
                diemChuan = cuoi.getDiemXettuyen();
            }
            diemTrungTuyenTheoNganh.put(entry.getKey(), diemChuan);
        }

        // Cap nhat diem_trung_tuyen trong entity dang nam tren RAM de log/hien thi dung ngay.
        for (Nganh n : nganhTrongDot.values()) {
            if (n != null && n.getNganhId() != null) {
                n.setDiemTrungTuyen(diemTrungTuyenTheoNganh.get(n.getNganhId()));
            }
        }

        Map<String, Integer> soTrungTuyenTheoNganh = new LinkedHashMap<>();
        List<KetQuaXetTuyen> chiTiet = new ArrayList<>();

        for (NguyenVong nv : ds) {
            String ketQua;
            String note;

            Integer thisinhId = layThiSinhId(nv);
            NguyenVong nvTrung = thisinhId != null ? nvTrungTuyenTheoThiSinh.get(thisinhId) : null;
            boolean isTrung = nv != null
                    && nv.getNguyenvongId() != null
                    && idNguyenVongTrung.contains(nv.getNguyenvongId());

            if (isTrung) {
                ketQua = NguyenVong.KetQua.TRUNG_TUYEN;
                note = "Trung tuyen trong chi tieu nganh; diem chuan nganh = " + formatDiemLog(layDiemTrungTuyen(nv, diemTrungTuyenTheoNganh));

                String tenNhom = tenNganhLog(nv);
                soTrungTuyenTheoNganh.put(tenNhom, soTrungTuyenTheoNganh.getOrDefault(tenNhom, 0) + 1);
            } else {
                ketQua = NguyenVong.KetQua.TRUOT;
                note = lyDoTruotSauXetChiTieu(nv, nvTrung, diemTrungTuyenTheoNganh);
            }

            nv.setKetQua(ketQua);
            nv.setGhiChu(appendNote(nv.getGhiChu(), "XET_THEO_CHI_TIEU: " + note));

            KetQuaXetTuyen item = new KetQuaXetTuyen(
                    nv,
                    ketQuaTuNguyenVong(nv),
                    ketQua,
                    nv.getNganh() != null ? nv.getNganh().getDiemSan() : null,
                    layChiTieuNganh(nv),
                    tinhHangTrongNganh(nv, ds)
            );
            item.ghiChu = note;
            chiTiet.add(item);
        }

        // Ghi diem + ket_qua xuong DB mot lan theo lo.
        nguyenVongDao.updateXetTuyenBatch(ds);

        // Cap nhat diem_trung_tuyen vao xt_nganh.
        nganhDao.updateDiemTrungTuyenBatch(diemTrungTuyenTheoNganh);

        // Cap nhat so_luong_hien_tai theo nganh/phuong thuc neu bang xt_nganh_phuongthuc co du lieu.
        capNhatSoLuongHienTaiTheoNhom(trungTuyenTheoNhomPhuongThuc);

        chiTiet.sort((a, b) -> {
            int cmpTs = tenThiSinh(a.nguyenVong).compareToIgnoreCase(tenThiSinh(b.nguyenVong));
            if (cmpTs != 0) return cmpTs;

            Integer ta = a.nguyenVong.getThuTu() != null
                    ? a.nguyenVong.getThuTu()
                    : Integer.MAX_VALUE;
            Integer tb = b.nguyenVong.getThuTu() != null
                    ? b.nguyenVong.getThuTu()
                    : Integer.MAX_VALUE;
            return ta.compareTo(tb);
        });

        result.danhSach.addAll(chiTiet);
        result.thongKeTheoNhom.putAll(soTrungTuyenTheoNganh);

        for (KetQuaXetTuyen kq : chiTiet) {
            if (NguyenVong.KetQua.TRUNG_TUYEN.equals(kq.ketQua)) {
                result.soTrungTuyen++;
            } else if (NguyenVong.KetQua.TRUOT.equals(kq.ketQua)) {
                result.soTruot++;
            } else if (NguyenVong.KetQua.CHO_XET.equals(kq.ketQua)) {
                result.soChoXet++;
            } else if (NguyenVong.KetQua.PHOI_DU_KIEN.equals(kq.ketQua)) {
                result.soPhoiDuKien++;
            }
        }

        return result;
    }

    /** Chay toan bo tat ca nganh cua mot phuong thuc theo cach cu. Giu lai cho tuong thich. */
    public List<DotXetTuyenResult> xetTuyenTheoPhuongThuc(Short phuongthucId) {
        List<Nganh> nganhs = nganhDao.findActive();
        List<DotXetTuyenResult> results = new ArrayList<>();
        for (Nganh n : nganhs) {
            try {
                results.add(xetTuyenNganhPhuongThuc(n.getNganhId(), phuongthucId));
            } catch (Exception e) {
                // log va tiep tuc
            }
        }
        return results;
    }

    /** Chay hoan chinh theo nganh/phuong thuc. Giu lai cho tuong thich. */
    public DotXetTuyenResult chayHoanChinh(Integer nganhId, Short phuongthucId, List<NguyenVong> nvList) {
        for (NguyenVong nv : nvList) tinhDiemNguyenVong(nv);
        return xetTuyenNganhPhuongThuc(nganhId, phuongthucId);
    }

    // ================================================================
    // Helper
    // ================================================================

    private void ganKetQuaDiemVaoNguyenVong(NguyenVong nv, TinhDiemService.KetQuaDiem kq) {
        if (nv == null || kq == null) return;
        nv.setDiemThxt(kq.diemThxt);
        nv.setDiemCong(kq.diemCong);
        nv.setDiemUutien(kq.diemUutien);
        nv.setDiemXettuyen(kq.diemXettuyen);
        nv.setPhuongThucDiemTotNhat(kq.phuongThucDiemTotNhat);
        nv.setGhiChu(kq.ghiChu);
    }

    private List<NguyenVong> locNguyenVong(List<NguyenVong> input, Short phuongthucIdFilter, Integer nganhIdFilter) {
        List<NguyenVong> out = new ArrayList<>();
        for (NguyenVong nv : input) {
            if (nv == null) continue;
            if (phuongthucIdFilter != null) {
                if (nv.getPhuongThuc() == null || !Objects.equals(nv.getPhuongThuc().getPhuongthucId(), phuongthucIdFilter)) {
                    continue;
                }
            }
            if (nganhIdFilter != null) {
                if (nv.getNganh() == null || !Objects.equals(nv.getNganh().getNganhId(), nganhIdFilter)) {
                    continue;
                }
            }
            out.add(nv);
        }
        return out;
    }

    private TinhDiemService.KetQuaDiem ketQuaTuNguyenVong(NguyenVong nv) {
        TinhDiemService.KetQuaDiem kq = new TinhDiemService.KetQuaDiem();
        kq.diemThxt = nv.getDiemThxt();
        kq.diemCong = nv.getDiemCong();
        kq.diemUutien = nv.getDiemUutien();
        kq.diemXettuyen = nv.getDiemXettuyen();
        kq.phuongThucDiemTotNhat = nv.getPhuongThucDiemTotNhat();
        kq.ghiChu = nv.getGhiChu();
        return kq;
    }

    private int compareTheoDiem(NguyenVong a, NguyenVong b) {
        BigDecimal da = a != null ? a.getDiemXettuyen() : null;
        BigDecimal db = b != null ? b.getDiemXettuyen() : null;
        if (da == null && db == null) return 0;
        if (da == null) return 1;
        if (db == null) return -1;
        int cmp = db.compareTo(da);
        if (cmp != 0) return cmp;

        Integer thuTuA = a.getThuTu() != null ? a.getThuTu() : Integer.MAX_VALUE;
        Integer thuTuB = b.getThuTu() != null ? b.getThuTu() : Integer.MAX_VALUE;
        cmp = thuTuA.compareTo(thuTuB);
        if (cmp != 0) return cmp;

        Integer idA = a.getNguyenvongId() != null ? a.getNguyenvongId() : Integer.MAX_VALUE;
        Integer idB = b.getNguyenvongId() != null ? b.getNguyenvongId() : Integer.MAX_VALUE;
        return idA.compareTo(idB);
    }

    private boolean coDiemHopLe(NguyenVong nv) {
        return nv != null && nv.getDiemXettuyen() != null && nv.getDiemXettuyen().compareTo(ZERO) > 0;
    }

    private boolean datDiemSan(NguyenVong nv) {
        if (nv == null || !coDiemHopLe(nv)) return false;
        BigDecimal diemSan = nv.getNganh() != null ? nv.getNganh().getDiemSan() : null;
        return diemSan == null || nv.getDiemXettuyen().compareTo(diemSan) >= 0;
    }

    private Integer layChiTieu(NguyenVong nv) {
        if (nv == null || nv.getNganh() == null) return 0;
        return layChiTieu(nv.getNganh(), nv.getPhuongThuc());
    }

    private Integer layChiTieu(Nganh nganh, PhuongThuc phuongThuc) {
        Integer chiTieu = null;
        if (nganh != null && phuongThuc != null && phuongThuc.getPhuongthucId() != null) {
            NganhPhuongThuc npt = nganhPhuongThucDao.findByNganhAndPhuongThuc(
                    nganh.getNganhId(), phuongThuc.getPhuongthucId());
            if (npt != null && npt.getChiTieu() != null) chiTieu = npt.getChiTieu();
        }
        if (chiTieu == null && nganh != null) chiTieu = nganh.getChiTieu();
        return chiTieu != null ? chiTieu : 0;
    }

    private int layChiTieuNganh(NguyenVong nv) {
        if (nv == null || nv.getNganh() == null || nv.getNganh().getChiTieu() == null) return 0;
        return Math.max(0, nv.getNganh().getChiTieu());
    }

    private Integer layThiSinhId(NguyenVong nv) {
        if (nv == null || nv.getThiSinh() == null) return null;
        return nv.getThiSinh().getThisinhId();
    }

    private BigDecimal layDiemTrungTuyen(NguyenVong nv, Map<Integer, BigDecimal> diemTrungTuyenTheoNganh) {
        if (nv == null || nv.getNganh() == null || nv.getNganh().getNganhId() == null) return null;
        return diemTrungTuyenTheoNganh.get(nv.getNganh().getNganhId());
    }

    private String formatDiemLog(BigDecimal d) {
        if (d == null) return "chua co";
        return d.setScale(3, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private String tenNganhLog(NguyenVong nv) {
        if (nv == null || nv.getNganh() == null) return "Nganh ?";
        String ma = nv.getNganh().getMaNganh() != null ? nv.getNganh().getMaNganh() : "?";
        String ten = nv.getNganh().getTenNganh() != null ? nv.getNganh().getTenNganh() : "";
        return ma + (ten.isBlank() ? "" : " - " + ten);
    }

    private String lyDoTruotSauXetChiTieu(NguyenVong nv,
                                          NguyenVong nvTrung,
                                          Map<Integer, BigDecimal> diemTrungTuyenTheoNganh) {
        if (nv == null) return "Khong trung tuyen";

        if (!coDiemHopLe(nv)) {
            return "Khong trung tuyen do khong co diem xet tuyen hop le";
        }

        if (!datDiemSan(nv)) {
            return "Khong trung tuyen do duoi diem san";
        }

        if (layChiTieuNganh(nv) <= 0) {
            return "Khong trung tuyen do nganh chua co chi tieu";
        }

        if (nvTrung != null && nvTrung.getThuTu() != null && nv.getThuTu() != null
                && nv.getThuTu() > nvTrung.getThuTu()) {
            return "Khong xet do da trung tuyen nguyen vong cao hon: NV" + nvTrung.getThuTu();
        }

        BigDecimal diemChuan = layDiemTrungTuyen(nv, diemTrungTuyenTheoNganh);
        BigDecimal diemXt = nv.getDiemXettuyen();

        if (diemChuan != null && diemXt != null) {
            int cmp = diemXt.compareTo(diemChuan);
            if (cmp < 0) {
                return "Khong trung tuyen do diem xet tuyen thap hon diem chuan nganh " + formatDiemLog(diemChuan);
            }
            if (cmp == 0) {
                return "Khong trung tuyen do bang diem chuan nhung thua tieu chi phu/qua chi tieu";
            }
        }

        return "Khong trung tuyen do khong nam trong chi tieu nganh";
    }

    private String nhomXetTuyenKey(NguyenVong nv) {
        Integer nganhId = nv != null && nv.getNganh() != null ? nv.getNganh().getNganhId() : 0;
        Short ptId = nv != null && nv.getPhuongThuc() != null ? nv.getPhuongThuc().getPhuongthucId() : 0;
        return nganhId + "_" + ptId;
    }

    private int tinhHangTrongNhom(NguyenVong nv, List<NguyenVong> all) {
        if (nv == null) return 0;
        String key = nhomXetTuyenKey(nv);
        List<NguyenVong> same = new ArrayList<>();
        for (NguyenVong item : all) {
            if (key.equals(nhomXetTuyenKey(item)) && coDiemHopLe(item)) same.add(item);
        }
        same.sort(this::compareTheoDiem);
        for (int i = 0; i < same.size(); i++) {
            if (Objects.equals(same.get(i).getNguyenvongId(), nv.getNguyenvongId())) return i + 1;
        }
        return 0;
    }

    private int tinhHangTrongNganh(NguyenVong nv, List<NguyenVong> all) {
        if (nv == null || nv.getNganh() == null || nv.getNganh().getNganhId() == null) return 0;
        Integer nganhId = nv.getNganh().getNganhId();
        List<NguyenVong> same = new ArrayList<>();
        for (NguyenVong item : all) {
            if (item != null
                    && item.getNganh() != null
                    && Objects.equals(item.getNganh().getNganhId(), nganhId)
                    && coDiemHopLe(item)
                    && datDiemSan(item)) {
                same.add(item);
            }
        }
        same.sort(this::compareTheoDiem);
        for (int i = 0; i < same.size(); i++) {
            if (Objects.equals(same.get(i).getNguyenvongId(), nv.getNguyenvongId())) return i + 1;
        }
        return 0;
    }

    private void capNhatSoLuongHienTai(Nganh nganh, PhuongThuc phuongThuc, int soLuong) {
        if (nganh == null || phuongThuc == null || phuongThuc.getPhuongthucId() == null) return;
        NganhPhuongThuc npt = nganhPhuongThucDao.findByNganhAndPhuongThuc(
                nganh.getNganhId(), phuongThuc.getPhuongthucId());
        if (npt != null) {
            npt.setSoLuongHienTai(soLuong);
            nganhPhuongThucDao.update(npt);
        }
    }

    private void capNhatSoLuongHienTaiTheoNhom(Map<String, List<NguyenVong>> giuTheoNhom) {
        for (List<NguyenVong> list : giuTheoNhom.values()) {
            if (list == null || list.isEmpty()) continue;
            NguyenVong nv = list.get(0);
            capNhatSoLuongHienTai(nv.getNganh(), nv.getPhuongThuc(), list.size());
        }
    }

    private String appendNote(String oldNote, String newNote) {
        if (newNote == null || newNote.trim().isEmpty()) return oldNote;
        if (oldNote == null || oldNote.trim().isEmpty()) return newNote;
        if (oldNote.contains(newNote)) return oldNote;
        String merged = oldNote + " | " + newNote;
        return merged.length() > 255 ? merged.substring(0, 255) : merged;
    }

    private String tenThiSinh(NguyenVong nv) {
        if (nv == null || nv.getThiSinh() == null) return "";
        return nv.getThiSinh().getHoVaTen() != null ? nv.getThiSinh().getHoVaTen() : "";
    }
}