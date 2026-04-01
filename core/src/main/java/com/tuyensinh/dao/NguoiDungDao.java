package com.tuyensinh.dao;

import com.tuyensinh.entity.VaiTro;
import com.tuyensinh.entity.NguoiDung;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

public class NguoiDungDao extends BaseDao<NguoiDung> {

    @Override
    protected Class<NguoiDung> getEntityClass() {
        return NguoiDung.class;
    }

    public Optional<NguoiDung> findByUsername(String username) {
        try (Session session = getSession()) {
            @SuppressWarnings("unchecked")
            NguoiDung nd = (NguoiDung) session.createQuery(
                "FROM NguoiDung nd WHERE nd.username = :username")
                .setParameter("username", username)
                .setMaxResults(1)
                .uniqueResult();
            return Optional.ofNullable(nd);
        }
    }

    public Optional<NguoiDung> findByEmail(String email) {
        try (Session session = getSession()) {
            @SuppressWarnings("unchecked")
            NguoiDung nd = (NguoiDung) session.createQuery(
                "FROM NguoiDung nd WHERE nd.email = :email")
                .setParameter("email", email)
                .setMaxResults(1)
                .uniqueResult();
            return Optional.ofNullable(nd);
        }
    }

    @SuppressWarnings("unchecked")
    public List<NguoiDung> findByVaiTro(Short vaitroId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM NguoiDung nd WHERE nd.vaiTro.vaitroId = :vaitroId ORDER BY nd.username")
                .setParameter("vaitroId", vaitroId)
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<NguoiDung> findActiveUsers() {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM NguoiDung nd WHERE nd.isActive = true ORDER BY nd.username")
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<NguoiDung> searchByUsernameOrHoTen(String keyword) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM NguoiDung nd WHERE nd.username LIKE :kw OR nd.hoTen LIKE :kw ORDER BY nd.username")
                .setParameter("kw", "%" + keyword + "%")
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<NguoiDung> findByPage(int page, int pageSize) {
        try (Session session = getSession()) {
            return session.createQuery("FROM NguoiDung nd ORDER BY nd.username")
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
        }
    }

    public long countAll() {
        try (Session session = getSession()) {
            return (Long) session.createQuery("SELECT COUNT(*) FROM NguoiDung").getSingleResult();
        }
    }

    public void updatePassword(NguoiDung nd, String newPasswordHash) {
        try (Session session = getSession()) {
            session.beginTransaction();
            nd.setPasswordHash(newPasswordHash);
            session.update(nd);
            session.getTransaction().commit();
        }
    }

    public void toggleActive(NguoiDung nd) {
        try (Session session = getSession()) {
            session.beginTransaction();
            nd.setIsActive(!nd.getIsActive());
            session.update(nd);
            session.getTransaction().commit();
        }
    }
}
