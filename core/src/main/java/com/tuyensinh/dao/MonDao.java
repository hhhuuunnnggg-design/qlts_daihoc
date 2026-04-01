package com.tuyensinh.dao;

import com.tuyensinh.entity.Mon;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

public class MonDao extends BaseDao<Mon> {

    @Override
    protected Class<Mon> getEntityClass() {
        return Mon.class;
    }

    public Optional<Mon> findByMa(String maMon) {
        try (Session session = getSession()) {
            @SuppressWarnings("unchecked")
            Mon mon = (Mon) session.createQuery(
                "FROM Mon m WHERE m.maMon = :ma")
                .setParameter("ma", maMon)
                .setMaxResults(1)
                .uniqueResult();
            return Optional.ofNullable(mon);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Mon> findByLoaiMon(String loaiMon) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM Mon m WHERE m.loaiMon = :loai ORDER BY m.maMon")
                .setParameter("loai", loaiMon)
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Mon> findNangKhieuMon() {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM Mon m WHERE m.loaiMon = :loai ORDER BY m.maMon")
                .setParameter("loai", Mon.LoaiMon.NANG_KHIEU)
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Mon> findDanhGiaNangLuc() {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM Mon m WHERE m.loaiMon = :loai ORDER BY m.maMon")
                .setParameter("loai", Mon.LoaiMon.DANH_GIA_NANG_LUC)
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Mon> findMonHoc() {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM Mon m WHERE m.loaiMon = :loai ORDER BY m.maMon")
                .setParameter("loai", Mon.LoaiMon.MON_HOC)
                .getResultList();
        }
    }
}
