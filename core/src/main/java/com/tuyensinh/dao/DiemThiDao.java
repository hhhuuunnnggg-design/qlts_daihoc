package com.tuyensinh.dao;

import com.tuyensinh.entity.DiemThi;
import org.hibernate.Session;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class DiemThiDao extends BaseDao<DiemThi> {

    @Override
    protected Class<DiemThi> getEntityClass() {
        return DiemThi.class;
    }

    public Optional<DiemThi> findByThiSinhAndPhuongThuc(Integer thisinhId, Short phuongthucId, Short namTuyensinh) {
        try (Session session = getSession()) {
            @SuppressWarnings("unchecked")
            DiemThi dt = (DiemThi) session.createQuery(
                "FROM DiemThi dt WHERE dt.thiSinh.thisinhId = :tsid AND dt.phuongThuc.phuongthucId = :ptid AND dt.namTuyensinh = :nam")
                .setParameter("tsid", thisinhId)
                .setParameter("ptid", phuongthucId)
                .setParameter("nam", namTuyensinh)
                .setMaxResults(1)
                .uniqueResult();
            return Optional.ofNullable(dt);
        }
    }

    @SuppressWarnings("unchecked")
    public List<DiemThi> findByThiSinhId(Integer thisinhId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM DiemThi dt WHERE dt.thiSinh.thisinhId = :tsid ORDER BY dt.phuongThuc.phuongthucId")
                .setParameter("tsid", thisinhId)
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<DiemThi> findByPhuongThuc(Short phuongthucId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM DiemThi dt WHERE dt.phuongThuc.phuongthucId = :ptid ORDER BY dt.thiSinh.ten, dt.thiSinh.ho")
                .setParameter("ptid", phuongthucId)
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<DiemThi> findByPhuongThucAndPage(Short phuongthucId, int page, int pageSize) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM DiemThi dt WHERE dt.phuongThuc.phuongthucId = :ptid ORDER BY dt.thiSinh.ten, dt.thiSinh.ho")
                .setParameter("ptid", phuongthucId)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> thongKeDiemByPhuongThucMon(Short phuongthucId, Integer monId) {
        try (Session session = getSession()) {
            String hql = "SELECT AVG(dct.diemSudung), MIN(dct.diemSudung), MAX(dct.diemSudung), COUNT(dct) " +
                         "FROM DiemThi dt JOIN dt.danhSachDiemChiTiet dct " +
                         "WHERE dt.phuongThuc.phuongthucId = :ptid AND dct.mon.monId = :mid";
            if (monId != null) {
                return session.createQuery(hql)
                    .setParameter("ptid", phuongthucId)
                    .setParameter("mid", monId)
                    .getResultList();
            } else {
                return session.createQuery(hql.replace("AND dct.mon.monId = :mid", ""))
                    .setParameter("ptid", phuongthucId)
                    .getResultList();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> thongKeDiemTheoMon(Short phuongthucId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "SELECT dct.mon.maMon, dct.mon.tenMon, AVG(dct.diemSudung), MIN(dct.diemSudung), MAX(dct.diemSudung), COUNT(dct) " +
                "FROM DiemThi dt JOIN dt.danhSachDiemChiTiet dct " +
                "WHERE dt.phuongThuc.phuongthucId = :ptid " +
                "GROUP BY dct.mon.monId ORDER BY dct.mon.maMon")
                .setParameter("ptid", phuongthucId)
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<DiemThi> searchByCccdOrSoBaoDanh(String keyword, Short phuongthucId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM DiemThi dt WHERE (dt.thiSinh.cccd LIKE :kw OR dt.sobaodanh LIKE :kw OR dt.thiSinh.ho LIKE :kw2 OR dt.thiSinh.ten LIKE :kw2) " +
                "AND (:ptid IS NULL OR dt.phuongThuc.phuongthucId = :ptid) " +
                "ORDER BY dt.thiSinh.ten, dt.thiSinh.ho")
                .setParameter("kw", "%" + keyword + "%")
                .setParameter("kw2", "%" + keyword + "%")
                .setParameter("ptid", phuongthucId)
                .getResultList();
        }
    }
}
