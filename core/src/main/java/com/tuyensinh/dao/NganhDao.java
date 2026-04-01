package com.tuyensinh.dao;

import com.tuyensinh.entity.Nganh;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

public class NganhDao extends BaseDao<Nganh> {

    @Override
    protected Class<Nganh> getEntityClass() {
        return Nganh.class;
    }

    public Optional<Nganh> findByMa(String maNganh) {
        try (Session session = getSession()) {
            @SuppressWarnings("unchecked")
            Nganh n = (Nganh) session.createQuery(
                "FROM Nganh n WHERE n.maNganh = :ma")
                .setParameter("ma", maNganh)
                .setMaxResults(1)
                .uniqueResult();
            return Optional.ofNullable(n);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Nganh> findActive() {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM Nganh n WHERE n.isActive = true ORDER BY n.maNganh")
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Nganh> searchByMaOrTen(String keyword) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM Nganh n WHERE n.maNganh LIKE :kw OR n.tenNganh LIKE :kw ORDER BY n.maNganh")
                .setParameter("kw", "%" + keyword + "%")
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Nganh> findByPage(int page, int pageSize) {
        try (Session session = getSession()) {
            return session.createQuery("FROM Nganh n ORDER BY n.maNganh")
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
        }
    }

    public long countAll() {
        try (Session session = getSession()) {
            return (Long) session.createQuery("SELECT COUNT(*) FROM Nganh").getSingleResult();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> thongKeNganh() {
        try (Session session = getSession()) {
            return session.createQuery(
                "SELECT n.maNganh, n.tenNganh, n.chiTieu, n.diemSan, COUNT(np.nganhPhuongthucId) " +
                "FROM Nganh n LEFT JOIN n.danhSachNganhPhuongThuc np GROUP BY n.nganhId ORDER BY n.maNganh")
                .getResultList();
        }
    }
}
