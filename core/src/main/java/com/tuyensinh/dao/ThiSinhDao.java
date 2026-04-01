package com.tuyensinh.dao;

import com.tuyensinh.entity.ThiSinh;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

public class ThiSinhDao extends BaseDao<ThiSinh> {

    @Override
    protected Class<ThiSinh> getEntityClass() {
        return ThiSinh.class;
    }

    public Optional<ThiSinh> findByCccd(String cccd) {
        try (Session session = getSession()) {
            @SuppressWarnings("unchecked")
            ThiSinh ts = (ThiSinh) session.createQuery(
                "FROM ThiSinh ts WHERE ts.cccd = :cccd")
                .setParameter("cccd", cccd)
                .setMaxResults(1)
                .uniqueResult();
            return Optional.ofNullable(ts);
        }
    }

    public Optional<ThiSinh> findBySoBaoDanh(String sobaodanh) {
        try (Session session = getSession()) {
            @SuppressWarnings("unchecked")
            ThiSinh ts = (ThiSinh) session.createQuery(
                "FROM ThiSinh ts WHERE ts.sobaodanh = :sobaodanh")
                .setParameter("sobaodanh", sobaodanh)
                .setMaxResults(1)
                .uniqueResult();
            return Optional.ofNullable(ts);
        }
    }

    @SuppressWarnings("unchecked")
    public List<ThiSinh> searchByCccdOrHoTen(String keyword) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM ThiSinh ts WHERE ts.cccd LIKE :kw OR ts.ho LIKE :kw2 OR ts.ten LIKE :kw2 ORDER BY ts.ten, ts.ho")
                .setParameter("kw", "%" + keyword + "%")
                .setParameter("kw2", "%" + keyword + "%")
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<ThiSinh> findByPage(int page, int pageSize) {
        try (Session session = getSession()) {
            return session.createQuery("FROM ThiSinh ts ORDER BY ts.ten, ts.ho")
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<ThiSinh> findByPageWithSearch(String keyword, int page, int pageSize) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM ThiSinh ts WHERE ts.cccd LIKE :kw OR ts.ho LIKE :kw2 OR ts.ten LIKE :kw2 OR ts.sobaodanh LIKE :kw ORDER BY ts.ten, ts.ho")
                .setParameter("kw", "%" + keyword + "%")
                .setParameter("kw2", "%" + keyword + "%")
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
        }
    }

    public long countBySearch(String keyword) {
        try (Session session = getSession()) {
            if (keyword == null || keyword.trim().isEmpty()) {
                return (Long) session.createQuery("SELECT COUNT(*) FROM ThiSinh").getSingleResult();
            }
            return (Long) session.createQuery(
                "SELECT COUNT(*) FROM ThiSinh ts WHERE ts.cccd LIKE :kw OR ts.ho LIKE :kw2 OR ts.ten LIKE :kw2")
                .setParameter("kw", "%" + keyword + "%")
                .setParameter("kw2", "%" + keyword + "%")
                .getSingleResult();
        }
    }

    @SuppressWarnings("unchecked")
    public List<ThiSinh> findByNguoiDungId(Integer nguoidungId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM ThiSinh ts WHERE ts.nguoiDung.nguoidungId = :nguoidungId")
                .setParameter("nguoidungId", nguoidungId)
                .getResultList();
        }
    }

    public String generateSoBaoDanh() {
        try (Session session = getSession()) {
            String maxSo = (String) session.createQuery(
                "SELECT MAX(ts.sobaodanh) FROM ThiSinh ts WHERE ts.sobaodanh IS NOT NULL")
                .getSingleResult();
            if (maxSo == null) {
                return "TS0001";
            }
            int num = Integer.parseInt(maxSo.replace("TS", ""));
            return String.format("TS%04d", num + 1);
        }
    }
}
