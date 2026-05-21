package com.tuyensinh.dao;

import com.tuyensinh.entity.NguyenVong;
import com.tuyensinh.service.ThongKeTuyenSinhService;
import com.tuyensinh.util.HibernateUtil;

import javax.persistence.EntityManager;
import java.util.List;

public class ThongKeTuyenSinhDao {

    public Object[] thongKeTongQuan() {
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        try {
            Long tongThiSinh = em.createQuery(
                    "select count(ts) from ThiSinh ts", Long.class
            ).getSingleResult();

            Long tongNganh = em.createQuery(
                    "select count(n) from Nganh n", Long.class
            ).getSingleResult();

            Long tongNguyenVong = em.createQuery(
                    "select count(nv) from NguyenVong nv", Long.class
            ).getSingleResult();

            Long soTrungTuyen = em.createQuery(
                            "select count(nv) from NguyenVong nv where nv.ketQua = :ketQua", Long.class
                    ).setParameter("ketQua", NguyenVong.KetQua.TRUNG_TUYEN)
                    .getSingleResult();

            Long soTruot = em.createQuery(
                            "select count(nv) from NguyenVong nv where nv.ketQua = :ketQua", Long.class
                    ).setParameter("ketQua", NguyenVong.KetQua.TRUOT)
                    .getSingleResult();

            Long soChuaXet = em.createQuery(
                            "select count(nv) from NguyenVong nv " +
                                    "where nv.ketQua is null or nv.ketQua = :ketQua", Long.class
                    ).setParameter("ketQua", NguyenVong.KetQua.CHO_XET)
                    .getSingleResult();

            return new Object[]{
                    tongThiSinh,
                    tongNganh,
                    tongNguyenVong,
                    soTrungTuyen,
                    soTruot,
                    soChuaXet
            };
        } finally {
            em.close();
        }
    }

    public List<Object[]> thongKeTheoNganh() {
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        try {
            return em.createQuery(
                            "select " +
                                    "n.maNganh, " +
                                    "n.tenNganh, " +
                                    "n.chiTieu, " +
                                    "count(nv), " +
                                    "sum(case when nv.ketQua = :trungTuyen then 1 else 0 end), " +
                                    "sum(case when nv.ketQua = :truot then 1 else 0 end), " +
                                    "n.diemTrungTuyen " +
                                    "from Nganh n " +
                                    "left join n.danhSachNguyenVong nv " +
                                    "group by n.nganhId, n.maNganh, n.tenNganh, n.chiTieu, n.diemTrungTuyen " +
                                    "order by n.maNganh",
                            Object[].class
                    ).setParameter("trungTuyen", NguyenVong.KetQua.TRUNG_TUYEN)
                    .setParameter("truot", NguyenVong.KetQua.TRUOT)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Object[]> thongKeTheoPhuongThuc() {
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        try {
            /*
             * Thong ke theo phuong thuc THUC TE dung de tinh diem tot nhat.
             * Khong dung nv.phuongthuc_id vi cot do la phuong thuc goc khi import
             * nguyen vong, thuong tat ca dang la THPT. Sau khi xet tuyen, nguon
             * diem thuc te duoc luu tai nv.phuong_thuc_diem_tot_nhat = THPT/DGNL/VSAT.
             */
            return em.createNativeQuery(
                    "SELECT " +
                            "pt.ma_phuongthuc, " +
                            "pt.ten_phuongthuc, " +
                            "COUNT(nv.nguyenvong_id) AS tong_nguyen_vong, " +
                            "COALESCE(SUM(CASE WHEN nv.ket_qua = 'TRUNG_TUYEN' THEN 1 ELSE 0 END), 0) AS so_trung_tuyen, " +
                            "COALESCE(SUM(CASE WHEN nv.ket_qua = 'TRUOT' THEN 1 ELSE 0 END), 0) AS so_truot, " +
                            "AVG(nv.diem_xettuyen) AS diem_xt_trung_binh " +
                            "FROM xt_phuongthuc pt " +
                            "LEFT JOIN xt_nguyenvong nv " +
                            "ON UPPER(TRIM(nv.phuong_thuc_diem_tot_nhat)) = UPPER(TRIM(pt.ma_phuongthuc)) " +
                            "WHERE pt.ma_phuongthuc IN ('THPT', 'DGNL', 'VSAT') " +
                            "GROUP BY pt.phuongthuc_id, pt.ma_phuongthuc, pt.ten_phuongthuc " +
                            "ORDER BY FIELD(pt.ma_phuongthuc, 'THPT', 'DGNL', 'VSAT')"
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Object[]> topNganhNhieuNguyenVong(int limit) {
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        try {
            return em.createQuery(
                            "select " +
                                    "n.maNganh, " +
                                    "n.tenNganh, " +
                                    "count(nv) " +
                                    "from NguyenVong nv " +
                                    "join nv.nganh n " +
                                    "group by n.nganhId, n.maNganh, n.tenNganh " +
                                    "order by count(nv) desc, n.maNganh",
                            Object[].class
                    ).setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public java.util.List<ThongKeTuyenSinhService.ThongKeThiSinhGroupDto> thongKeThiSinhTheoDoiTuong() {
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        try {
            java.util.List<Object[]> rows = em.createNativeQuery(
                    "SELECT " +
                            "COALESCE(dt.ma_doituong, 'KHONG_CO') AS ma, " +
                            "COALESCE(dt.ten_doituong, 'Không có đối tượng ưu tiên') AS ten, " +
                            "COUNT(ts.thisinh_id) AS so_luong " +
                            "FROM xt_thisinh ts " +
                            "LEFT JOIN xt_doituong_uutien dt ON dt.doituong_id = ts.doituong_id " +
                            "GROUP BY dt.ma_doituong, dt.ten_doituong " +
                            "ORDER BY so_luong DESC, ma ASC"
            ).getResultList();

            java.util.List<ThongKeTuyenSinhService.ThongKeThiSinhGroupDto> result =
                    new java.util.ArrayList<>();

            for (Object[] row : rows) {
                result.add(new ThongKeTuyenSinhService.ThongKeThiSinhGroupDto(
                        row[0] != null ? row[0].toString() : "",
                        row[1] != null ? row[1].toString() : "",
                        row[2] != null ? (Number) row[2] : 0
                ));
            }

            return result;
        } finally {
            em.close();
        }
    }

    public java.util.List<ThongKeTuyenSinhService.ThongKeThiSinhGroupDto> thongKeThiSinhTheoKhuVuc() {
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        try {
            java.util.List<Object[]> rows = em.createNativeQuery(
                    "SELECT " +
                            "COALESCE(kv.ma_khuvuc, 'KHONG_CO') AS ma, " +
                            "COALESCE(kv.ten_khuvuc, 'Không có khu vực ưu tiên') AS ten, " +
                            "COUNT(ts.thisinh_id) AS so_luong " +
                            "FROM xt_thisinh ts " +
                            "LEFT JOIN xt_khuvuc_uutien kv ON kv.khuvuc_id = ts.khuvuc_id " +
                            "GROUP BY kv.ma_khuvuc, kv.ten_khuvuc " +
                            "ORDER BY so_luong DESC, ma ASC"
            ).getResultList();

            java.util.List<ThongKeTuyenSinhService.ThongKeThiSinhGroupDto> result =
                    new java.util.ArrayList<>();

            for (Object[] row : rows) {
                result.add(new ThongKeTuyenSinhService.ThongKeThiSinhGroupDto(
                        row[0] != null ? row[0].toString() : "",
                        row[1] != null ? row[1].toString() : "",
                        row[2] != null ? (Number) row[2] : 0
                ));
            }

            return result;
        } finally {
            em.close();
        }
    }

    public Object[] findThiSinhDetail(String key) {
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        try {
            java.util.List<Object[]> rows = em.createNativeQuery(
                            "SELECT " +
                                    "ts.thisinh_id, " +
                                    "ts.cccd, " +
                                    "ts.sobaodanh, " +
                                    "CONCAT(ts.ho, ' ', ts.ten) AS ho_ten, " +
                                    "ts.ngay_sinh, " +
                                    "ts.gioi_tinh, " +
                                    "COALESCE(dt.ma_doituong, '') AS doi_tuong, " +
                                    "COALESCE(kv.ma_khuvuc, '') AS khu_vuc " +
                                    "FROM xt_thisinh ts " +
                                    "LEFT JOIN xt_doituong_uutien dt ON dt.doituong_id = ts.doituong_id " +
                                    "LEFT JOIN xt_khuvuc_uutien kv ON kv.khuvuc_id = ts.khuvuc_id " +
                                    "WHERE ts.cccd = :key OR ts.sobaodanh = :key " +
                                    "LIMIT 1"
                    )
                    .setParameter("key", key)
                    .getResultList();

            return rows.isEmpty() ? null : rows.get(0);
        } finally {
            em.close();
        }
    }

    public java.util.List<Object[]> findDiemThiSinh(String key) {
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        try {
            return em.createNativeQuery(
                            "SELECT " +
                                    "pt.ma_phuongthuc AS phuong_thuc, " +
                                    "dt.nam_tuyensinh, " +
                                    "dt.sobaodanh, " +
                                    "m.ma_mon, " +
                                    "m.ten_mon, " +
                                    "ct.diem_goc, " +
                                    "ct.diem_quydoi, " +
                                    "ct.diem_sudung " +
                                    "FROM xt_thisinh ts " +
                                    "JOIN xt_diemthi dt ON dt.thisinh_id = ts.thisinh_id " +
                                    "JOIN xt_phuongthuc pt ON pt.phuongthuc_id = dt.phuongthuc_id " +
                                    "LEFT JOIN xt_diemthi_chitiet ct ON ct.diemthi_id = dt.diemthi_id " +
                                    "LEFT JOIN xt_mon m ON m.mon_id = ct.mon_id " +
                                    "WHERE (ts.cccd = :key OR ts.sobaodanh = :key) " +
                                    "AND pt.ma_phuongthuc IN ('THPT', 'DGNL', 'VSAT') " +
                                    "ORDER BY FIELD(pt.ma_phuongthuc, 'THPT', 'DGNL', 'VSAT'), m.ma_mon"
                    )
                    .setParameter("key", key)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public java.util.List<Object[]> thongKeNganhTuyenSinhChiTiet() {
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        try {
            return em.createNativeQuery(
                    "SELECT " +
                            "n.ma_nganh, " +
                            "n.ten_nganh, " +
                            "n.chi_tieu, " +
                            "n.diem_san, " +
                            "n.diem_trung_tuyen, " +
                            "GROUP_CONCAT(DISTINCT pt.ma_phuongthuc ORDER BY pt.ma_phuongthuc SEPARATOR ', ') AS phuong_thuc, " +
                            "COUNT(DISTINCT nv.nguyenvong_id) AS tong_nguyen_vong, " +
                            "SUM(CASE WHEN UPPER(nv.ket_qua) = 'TRUNG_TUYEN' THEN 1 ELSE 0 END) AS so_trung_tuyen " +
                            "FROM xt_nganh n " +
                            "LEFT JOIN xt_nganh_phuongthuc np ON np.nganh_id = n.nganh_id " +
                            "LEFT JOIN xt_phuongthuc pt ON pt.phuongthuc_id = np.phuongthuc_id " +
                            "LEFT JOIN xt_nguyenvong nv ON nv.nganh_id = n.nganh_id " +
                            "GROUP BY n.nganh_id, n.ma_nganh, n.ten_nganh, n.chi_tieu, n.diem_san, n.diem_trung_tuyen " +
                            "ORDER BY n.ma_nganh ASC"
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public java.util.List<Object[]> danhSachTrungTuyenTheoNganh() {
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        try {
            return em.createNativeQuery(
                    "SELECT " +
                            "n.ma_nganh, " +
                            "n.ten_nganh, " +
                            "ts.cccd, " +
                            "CONCAT(ts.ho, ' ', ts.ten) AS ho_ten, " +
                            "ts.sobaodanh, " +
                            "pt.ma_phuongthuc, " +
                            "nv.thu_tu, " +
                            "nv.diem_xettuyen, " +
                            "nv.ket_qua " +
                            "FROM xt_nguyenvong nv " +
                            "JOIN xt_thisinh ts ON ts.thisinh_id = nv.thisinh_id " +
                            "JOIN xt_nganh n ON n.nganh_id = nv.nganh_id " +
                            "JOIN xt_phuongthuc pt ON pt.phuongthuc_id = nv.phuongthuc_id " +
                            "WHERE UPPER(nv.ket_qua) = 'TRUNG_TUYEN' " +
                            "ORDER BY n.ma_nganh ASC, nv.diem_xettuyen DESC, ts.sobaodanh ASC"
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public java.util.List<Object[]> soLuongTrungTuyenTheoNganhPhuongThuc() {
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        try {
            return em.createNativeQuery(
                    "SELECT " +
                            "n.ma_nganh, " +
                            "n.ten_nganh, " +
                            "n.chi_tieu, " +
                            "SUM(CASE WHEN pt.ma_phuongthuc = 'THPT' AND UPPER(nv.ket_qua) = 'TRUNG_TUYEN' THEN 1 ELSE 0 END) AS thpt, " +
                            "SUM(CASE WHEN pt.ma_phuongthuc = 'DGNL' AND UPPER(nv.ket_qua) = 'TRUNG_TUYEN' THEN 1 ELSE 0 END) AS dgnl, " +
                            "SUM(CASE WHEN pt.ma_phuongthuc = 'VSAT' AND UPPER(nv.ket_qua) = 'TRUNG_TUYEN' THEN 1 ELSE 0 END) AS vsat, " +
                            "SUM(CASE WHEN UPPER(nv.ket_qua) = 'TRUNG_TUYEN' THEN 1 ELSE 0 END) AS tong_trung_tuyen " +
                            "FROM xt_nganh n " +
                            "LEFT JOIN xt_nguyenvong nv ON nv.nganh_id = n.nganh_id " +
                            "LEFT JOIN xt_phuongthuc pt ON pt.phuongthuc_id = nv.phuongthuc_id " +
                            "GROUP BY n.nganh_id, n.ma_nganh, n.ten_nganh, n.chi_tieu " +
                            "ORDER BY tong_trung_tuyen DESC, n.ma_nganh ASC"
            ).getResultList();
        } finally {
            em.close();
        }
    }
}