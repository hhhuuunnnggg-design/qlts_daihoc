package com.tuyensinh.dao;

import com.tuyensinh.entity.DiemCong;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

public class DiemCongDao extends BaseDao<DiemCong> {

    @Override
    protected Class<DiemCong> getEntityClass() {
        return DiemCong.class;
    }

    @SuppressWarnings("unchecked")
    public List<DiemCong> findByThiSinhId(Integer thisinhId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM DiemCong dc WHERE dc.thiSinh.thisinhId = :tsid ORDER BY dc.nganhToHop.nganh.maNganh")
                .setParameter("tsid", thisinhId)
                .getResultList();
        }
    }

    public Optional<DiemCong> findByThiSinhNganhToHopPhuongThuc(Integer thisinhId, Integer nganhToHopId, Short phuongthucId) {
        try (Session session = getSession()) {
            @SuppressWarnings("unchecked")
            DiemCong dc = (DiemCong) session.createQuery(
                "FROM DiemCong dc WHERE dc.thiSinh.thisinhId = :tsid AND dc.nganhToHop.nganhTohopId = :ntid AND dc.phuongThuc.phuongthucId = :ptid")
                .setParameter("tsid", thisinhId)
                .setParameter("ntid", nganhToHopId)
                .setParameter("ptid", phuongthucId)
                .setMaxResults(1)
                .uniqueResult();
            return Optional.ofNullable(dc);
        }
    }

    @SuppressWarnings("unchecked")
    public List<DiemCong> findAll() {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM DiemCong dc ORDER BY dc.thiSinh.ten, dc.thiSinh.ho")
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<DiemCong> findByPhuongThuc(Short phuongthucId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM DiemCong dc WHERE dc.phuongThuc.phuongthucId = :ptid ORDER BY dc.thiSinh.ten")
                .setParameter("ptid", phuongthucId)
                .getResultList();
        }
    }
}
