package com.tuyensinh.dao;

import com.tuyensinh.entity.NguyenVong;
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
            return em.createQuery(
                            "select " +
                                    "pt.maPhuongthuc, " +
                                    "pt.tenPhuongthuc, " +
                                    "count(nv), " +
                                    "sum(case when nv.ketQua = :trungTuyen then 1 else 0 end), " +
                                    "sum(case when nv.ketQua = :truot then 1 else 0 end), " +
                                    "avg(nv.diemXettuyen) " +
                                    "from NguyenVong nv " +
                                    "join nv.phuongThuc pt " +
                                    "group by pt.phuongthucId, pt.maPhuongthuc, pt.tenPhuongthuc " +
                                    "order by pt.phuongthucId",
                            Object[].class
                    ).setParameter("trungTuyen", NguyenVong.KetQua.TRUNG_TUYEN)
                    .setParameter("truot", NguyenVong.KetQua.TRUOT)
                    .getResultList();
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
}