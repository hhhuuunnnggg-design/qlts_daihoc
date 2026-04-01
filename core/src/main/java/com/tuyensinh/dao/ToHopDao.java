package com.tuyensinh.dao;

import com.tuyensinh.entity.ToHop;
import com.tuyensinh.entity.ToHopMon;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

public class ToHopDao extends BaseDao<ToHop> {

    @Override
    protected Class<ToHop> getEntityClass() {
        return ToHop.class;
    }

    public Optional<ToHop> findByMa(String maTohop) {
        try (Session session = getSession()) {
            @SuppressWarnings("unchecked")
            ToHop th = (ToHop) session.createQuery(
                "FROM ToHop th WHERE th.maTohop = :ma")
                .setParameter("ma", maTohop)
                .setMaxResults(1)
                .uniqueResult();
            return Optional.ofNullable(th);
        }
    }

    @SuppressWarnings("unchecked")
    public List<ToHop> findAll() {
        try (Session session = getSession()) {
            return session.createQuery("FROM ToHop th ORDER BY th.maTohop").getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<ToHopMon> findMonByToHopId(Integer tohopId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM ToHopMon tm WHERE tm.toHop.tohopId = :id ORDER BY tm.thuTu")
                .setParameter("id", tohopId)
                .getResultList();
        }
    }

    public void saveToHopMon(ToHopMon entity) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.saveOrUpdate(entity);
            session.getTransaction().commit();
        }
    }

    @SuppressWarnings("unchecked")
    public List<ToHop> searchByMaOrTen(String keyword) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM ToHop th WHERE th.maTohop LIKE :kw OR th.tenTohop LIKE :kw ORDER BY th.maTohop")
                .setParameter("kw", "%" + keyword + "%")
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<ToHop> findNangKhieuToHop() {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM ToHop th WHERE th.maTohop LIKE 'NK%' ORDER BY th.maTohop")
                .getResultList();
        }
    }
}
