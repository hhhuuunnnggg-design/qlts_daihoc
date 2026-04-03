package com.tuyensinh.service;

import com.tuyensinh.dao.*;
import com.tuyensinh.entity.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Batch xet tuyen: tinh diem + danh gia ket qua cho nhieu nguyen vong.
 * <p>
 * Quy trinh 2 giai doan:
 * <ol>
 * <li>Tinh diem: goi TinhDiemService.tinhDiem() cho tung nguyen vong
 * <li>Xet tuyen: sap xep theo diem, ap dung chi tieu, danh gia ket qua
 * </ol>
 * <p>
 * Co the goi tung giai doan rieng biet hoac goi chayHet() de thuc hien ca hai.
 */
public class XetTuyenEngine {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final TinhDiemService tinhDiemService = new TinhDiemService();
    private final DiemThiDao diemThiDao = new DiemThiDao();
    private final DiemCongDao diemCongDao = new DiemCongDao();
    private final NguyenVongDao nguyenVongDao = new NguyenVongDao();
    private final NganhPhuongThucDao nganhPhuongThucDao = new NganhPhuongThucDao();
    private final NganhDao nganhDao = new NganhDao();
    private final PhuongThucDao phuongThucDao = new PhuongThucDao();

    /**
     * Ket qua chi tiet cua mot nguyen vong sau khi xet tuyen.
     */
    public static class KetQuaXetTuyen {
        public final NguyenVong nguyenVong;
        public final TinhDiemService.KetQuaDiem ketQuaDiem;
        public final String ketQua;       // CHO_XET | TRUNG_TUYEN | TRUOT | PHOI_DU_KIEN
        public final BigDecimal diemSan; // diem san cua nganh
        public final Integer chiTieu;     // chi tieu cua nganh-phuongthuc
        public final int thuTuHienTai;    // vi tri trong danh sach da sap xep
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
            return String.format("[%s] %s | THXT=%.2f | Cong=%.2f | XT=%.2f | SL=%d/%d",
                ketQua,
                nguyenVong.getThiSinh() != null ? nguyenVong.getThiSinh().getHoVaTen() : "?",
                ketQuaDiem != null ? ketQuaDiem.diemThxt : ZERO,
                ketQuaDiem != null ? ketQuaDiem.diemCong : ZERO,
                ketQuaDiem != null ? ketQuaDiem.diemXettuyen : ZERO,
                thuTuHienTai, chiTieu != null ? chiTieu : 0);
        }
    }

    /**
     * Ket qua tong hop cua mot dot xet tuyen.
     */
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

    // ══════════════════════════════════════════════════════════════════════
    //  GIAI DOAN 1 — Tinh diem cho mot nguyen vong
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Tinh diem cho mot nguyen vong, cap nhat cac truong diem_thxt / diem_cong /
     * diem_uutien / diem_xettuyen len NguyenVong.
     *
     * @return KetQuaDiem da tinh, hoac null neu khong the tinh
     */
    public TinhDiemService.KetQuaDiem tinhDiemNguyenVong(NguyenVong nv) {
        if (nv.getThiSinh() == null || nv.getPhuongThuc() == null) return null;

        PhuongThuc pt = nv.getPhuongThuc();
        ThiSinh ts = nv.getThiSinh();

        // Tim DiemThi
        DiemThi diemThi = diemThiDao
            .findByThiSinhAndPhuongThuc(ts.getThisinhId(), pt.getPhuongthucId(), null)
            .orElse(null);

        // Tim DiemCong
        Optional<DiemCong> diemCong = Optional.empty();
        if (nv.getNganhToHop() != null) {
            diemCong = diemCongDao.findByThiSinhNganhToHopPhuongThuc(
                ts.getThisinhId(),
                nv.getNganhToHop().getNganhTohopId(),
                pt.getPhuongthucId()
            );
        }

        // Tinh
        TinhDiemService.KetQuaDiem kq = tinhDiemService.tinhDiem(nv, diemThi, diemCong);

        // Cap nhat NguyenVong
        nv.setDiemThxt(kq.diemThxt);
        nv.setDiemCong(kq.diemCong);
        nv.setDiemUutien(kq.diemUutien);
        nv.setDiemXettuyen(kq.diemXettuyen);
        nguyenVongDao.update(nv);

        return kq;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  GIAI DOAN 2 — Xet tuyen theo (nganh, phuongthuc)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Xet tuyen cho mot nganh + phuong thuc.
     * <p>
     * Quy tac:
     * <ul>
     *   <li>Sap xep theo diem_xettuyen giam dan</li>
     *   <li>Diem >= diem_san cua nganh</li>
     *   <li>Chi tieu lay tu NganhPhuongThuc (neu co), neu khong thi tu Nganh.chiTieu</li>
     *   <li>Top chi_tieu danh dau TRUNG_TUYEN, con lai la TRUOT</li>
     *   <li>Diem = null thi danh dau CHO_XET</li>
     *   <li>Diem >= diem_san nhung vuot qua chi tieu thi PHOI_DU_KIEN</li>
     * </ul>
     */
    public DotXetTuyenResult xetTuyenNganhPhuongThuc(Integer nganhId, Short phuongthucId) {
        Nganh nganh = nganhDao.findById(nganhId);
        if (nganh == null) throw new IllegalArgumentException("Nganh not found: " + nganhId);

        // Lay PhuongThuc tu id
        PhuongThuc phuongThuc = null;
        if (phuongthucId != null) {
            phuongThuc = phuongThucDao.findById((int) phuongthucId);
        }

        // Lay chi tieu
        Integer chiTieu = null;
        if (phuongthucId != null) {
            NganhPhuongThuc npt = nganhPhuongThucDao.findByNganhAndPhuongThuc(nganhId, phuongthucId);
            if (npt != null && npt.getChiTieu() != null) {
                chiTieu = npt.getChiTieu();
            }
        }
        if (chiTieu == null) chiTieu = nganh.getChiTieu();
        if (chiTieu == null) chiTieu = 0;

        BigDecimal diemSan = nganh.getDiemSan();

        // Lay tat ca nguyen vong
        List<NguyenVong> nvs = (phuongthucId != null)
            ? nguyenVongDao.findByNganhIdAndPhuongThuc(nganhId, phuongthucId)
            : nguyenVongDao.findByNganhId(nganhId);

        // Sap xep theo diem
        nvs.sort((a, b) -> {
            BigDecimal da = a.getDiemXettuyen();
            BigDecimal db = b.getDiemXettuyen();
            if (da == null && db == null) return 0;
            if (da == null) return 1;
            if (db == null) return -1;
            return db.compareTo(da); // giam dan
        });

        List<KetQuaXetTuyen> results = new ArrayList<>();
        int countTrungTuyen = 0;

        for (int i = 0; i < nvs.size(); i++) {
            NguyenVong nv = nvs.get(i);
            TinhDiemService.KetQuaDiem kqDiem = null;
            String ketQua;

            if (nv.getDiemXettuyen() == null) {
                ketQua = NguyenVong.KetQua.CHO_XET;
            } else {
                kqDiem = new TinhDiemService.KetQuaDiem();
                kqDiem.diemThxt = nv.getDiemThxt();
                kqDiem.diemCong = nv.getDiemCong();
                kqDiem.diemUutien = nv.getDiemUutien();
                kqDiem.diemXettuyen = nv.getDiemXettuyen();

                if (diemSan != null && nv.getDiemXettuyen().compareTo(diemSan) < 0) {
                    ketQua = NguyenVong.KetQua.TRUOT;
                } else if (chiTieu > 0 && countTrungTuyen < chiTieu) {
                    ketQua = NguyenVong.KetQua.TRUNG_TUYEN;
                    countTrungTuyen++;
                } else if (chiTieu > 0 && countTrungTuyen >= chiTieu) {
                    // Da day chieu, nhung van dat diem san → phoi du kien
                    if (diemSan != null && nv.getDiemXettuyen().compareTo(diemSan) >= 0) {
                        ketQua = NguyenVong.KetQua.PHOI_DU_KIEN;
                    } else {
                        ketQua = NguyenVong.KetQua.TRUOT;
                    }
                } else {
                    // chiTieu = 0 (khong co chi tieu), van dat san → trung tuyen
                    ketQua = (diemSan == null || nv.getDiemXettuyen().compareTo(diemSan) >= 0)
                        ? NguyenVong.KetQua.TRUNG_TUYEN
                        : NguyenVong.KetQua.TRUOT;
                }
            }

            nv.setKetQua(ketQua);
            nguyenVongDao.update(nv);

            results.add(new KetQuaXetTuyen(nv, kqDiem, ketQua, diemSan, chiTieu, i + 1));
        }

        // Cap nhat so_luong_hien_tai tren NganhPhuongThuc
        if (phuongthucId != null) {
            NganhPhuongThuc npt = nganhPhuongThucDao.findByNganhAndPhuongThuc(nganhId, phuongthucId);
            if (npt != null) {
                npt.setSoLuongHienTai(countTrungTuyen);
                nganhPhuongThucDao.update(npt);
            }
        }

        return new DotXetTuyenResult(nganh, phuongThuc, results);
    }

    /**
     * Xet tuyen tat ca nguyen vong cho mot phuong thuc (tat ca nganh).
     */
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

    /**
     * Chay hoan chinh giai doan 1 (tinh diem) roi giai doan 2 (xet tuyen) cho mot nganh + phuong thuc.
     */
    public DotXetTuyenResult chayHoanChinh(Integer nganhId, Short phuongthucId, List<NguyenVong> nvList) {
        // Giai doan 1: tinh diem
        for (NguyenVong nv : nvList) {
            tinhDiemNguyenVong(nv);
        }
        // Giai doan 2: xet tuyen
        return xetTuyenNganhPhuongThuc(nganhId, phuongthucId);
    }
}
