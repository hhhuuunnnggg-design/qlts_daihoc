package com.tuyensinh.dao;

import com.tuyensinh.entity.NguyenVong;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

public class NguyenVongDao extends BaseDao<NguyenVong> {

    @Override
    protected Class<NguyenVong> getEntityClass() {
        return NguyenVong.class;
    }

    @SuppressWarnings("unchecked")
    public List<NguyenVong> findByThiSinhId(Integer thisinhId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM NguyenVong nv WHERE nv.thiSinh.thisinhId = :tsid ORDER BY nv.thuTu")
                .setParameter("tsid", thisinhId)
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<NguyenVong> findByNganhId(Integer nganhId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM NguyenVong nv WHERE nv.nganh.nganhId = :nid ORDER BY nv.diemsxettuyen DESC")
                .setParameter("nid", nganhId)
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<NguyenVong> findByNganhIdAndPhuongThuc(Integer nganhId, Short phuongthucId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM NguyenVong nv WHERE nv.nganh.nganhId = :nid AND nv.phuongThuc.phuongthucId = :ptid ORDER BY nv.diemsxettuyen DESC")
                .setParameter("nid", nganhId)
                .setParameter("ptid", phuongthucId)
                .getResultList();
        }
    }

    public Optional<NguyenVong> findByThiSinhNganhToHopPhuongThuc(Integer thisinhId, Integer nganhId, Integer nganhToHopId, Short phuongthucId) {
        try (Session session = getSession()) {
            @SuppressWarnings("unchecked")
            NguyenVong nv = (NguyenVong) session.createQuery(
                "FROM NguyenVong nv WHERE nv.thiSinh.thisinhId = :tsid AND nv.nganh.nganhId = :nid AND nv.nganhToHop.nganhTohopId = :ntid AND nv.phuongThuc.phuongthucId = :ptid")
                .setParameter("tsid", thisinhId)
                .setParameter("nid", nganhId)
                .setParameter("ntid", nganhToHopId)
                .setParameter("ptid", phuongthucId)
                .setMaxResults(1)
                .uniqueResult();
            return Optional.ofNullable(nv);
        }
    }

    @SuppressWarnings("unchecked")
    public List<NguyenVong> findByKetQua(String ketQua) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM NguyenVong nv WHERE nv.ketQua = :kq ORDER BY nv.diemsxettuyen DESC")
                .setParameter("kq", ketQua)
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<NguyenVong> findAll() {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM NguyenVong nv ORDER BY nv.thiSinh.ten, nv.thiSinh.ho, nv.thuTu")
                .getResultList();
        }
    }

    public int countByNganhAndPhuongThuc(Integer nganhId, Short phuongthucId, String ketQua) {
        try (Session session = getSession()) {
            Long count = (Long) session.createQuery(
                "SELECT COUNT(*) FROM NguyenVong nv WHERE nv.nganh.nganhId = :nid AND nv.phuongThuc.phuongthucId = :ptid AND nv.ketQua = :kq")
                .setParameter("nid", nganhId)
                .setParameter("ptid", phuongthucId)
                .setParameter("kq", ketQua)
                .getSingleResult();
            return count.intValue();
        }
    }
}
