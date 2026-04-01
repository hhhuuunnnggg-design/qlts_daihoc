package com.tuyensinh.dao;

import com.tuyensinh.entity.PhuongThuc;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

public class PhuongThucDao extends BaseDao<PhuongThuc> {

    @Override
    protected Class<PhuongThuc> getEntityClass() {
        return PhuongThuc.class;
    }

    public Optional<PhuongThuc> findByMa(String maPhuongthuc) {
        try (Session session = getSession()) {
            @SuppressWarnings("unchecked")
            PhuongThuc pt = (PhuongThuc) session.createQuery(
                "FROM PhuongThuc pt WHERE pt.maPhuongthuc = :ma")
                .setParameter("ma", maPhuongthuc)
                .setMaxResults(1)
                .uniqueResult();
            return Optional.ofNullable(pt);
        }
    }

    @SuppressWarnings("unchecked")
    public List<PhuongThuc> findActive() {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM PhuongThuc pt WHERE pt.isActive = true ORDER BY pt.phuongthucId")
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<PhuongThuc> findAll() {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM PhuongThuc pt ORDER BY pt.phuongthucId")
                .getResultList();
        }
    }
}
