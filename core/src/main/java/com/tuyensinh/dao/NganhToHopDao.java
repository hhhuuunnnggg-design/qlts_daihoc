package com.tuyensinh.dao;

import com.tuyensinh.entity.NganhToHop;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

public class NganhToHopDao extends BaseDao<NganhToHop> {

    @Override
    protected Class<NganhToHop> getEntityClass() {
        return NganhToHop.class;
    }

    @SuppressWarnings("unchecked")
    public List<NganhToHop> findByNganhId(Integer nganhId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM NganhToHop nt WHERE nt.nganh.nganhId = :nid ORDER BY nt.toHop.maTohop")
                .setParameter("nid", nganhId)
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<NganhToHop> findByToHopId(Integer tohopId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM NganhToHop nt WHERE nt.toHop.tohopId = :tid ORDER BY nt.nganh.maNganh")
                .setParameter("tid", tohopId)
                .getResultList();
        }
    }

    public Optional<NganhToHop> findByNganhAndToHop(Integer nganhId, Integer tohopId) {
        try (Session session = getSession()) {
            @SuppressWarnings("unchecked")
            NganhToHop nt = (NganhToHop) session.createQuery(
                "FROM NganhToHop nt WHERE nt.nganh.nganhId = :nid AND nt.toHop.tohopId = :tid")
                .setParameter("nid", nganhId)
                .setParameter("tid", tohopId)
                .setMaxResults(1)
                .uniqueResult();
            return Optional.ofNullable(nt);
        }
    }

    @SuppressWarnings("unchecked")
    public List<NganhToHop> findAll() {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM NganhToHop nt ORDER BY nt.nganh.maNganh, nt.toHop.maTohop")
                .getResultList();
        }
    }
}
