package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.IToHopDao;
import com.tuyensinh.entity.ToHop;
import com.tuyensinh.entity.ToHopMon;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

public class ToHopDao extends BaseDao<ToHop> implements IToHopDao {

    @Override
    protected Class<ToHop> getEntityClass() {
        return ToHop.class;
    }

    public Optional<ToHop> findByMa(String maTohop) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ToHop> cq = cb.createQuery(ToHop.class);
        Root<ToHop> root = cq.from(ToHop.class);
        cq.select(root).where(cb.equal(root.get("maTohop"), maTohop));
        List<ToHop> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<ToHop> findAll() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ToHop> cq = cb.createQuery(ToHop.class);
        Root<ToHop> root = cq.from(ToHop.class);
        cq.select(root).orderBy(cb.asc(root.get("maTohop")));
        return em().createQuery(cq).getResultList();
    }

    public List<ToHopMon> findMonByToHopId(Integer tohopId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ToHopMon> cq = cb.createQuery(ToHopMon.class);
        Root<ToHopMon> root = cq.from(ToHopMon.class);
        Join<ToHopMon, ToHop> toHop = root.join("toHop");
        cq.select(root).where(cb.equal(toHop.get("tohopId"), tohopId));
        cq.orderBy(cb.asc(root.get("thuTu")));
        return em().createQuery(cq).getResultList();
    }

    public void saveToHopMon(ToHopMon entity) {
        EntityManager em = em();
        em.getTransaction().begin();
        try {
            em.unwrap(org.hibernate.Session.class).saveOrUpdate(entity);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void deleteToHopMon(ToHopMon entity) {
        EntityManager em = em();
        em.getTransaction().begin();
        try {
            em.remove(em.contains(entity) ? entity : em.merge(entity));
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public List<ToHop> searchByMaOrTen(String keyword) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ToHop> cq = cb.createQuery(ToHop.class);
        Root<ToHop> root = cq.from(ToHop.class);
        String kw = "%" + keyword + "%";
        Predicate p1 = cb.like(root.get("maTohop"), kw);
        Predicate p2 = cb.like(root.get("tenTohop"), kw);
        cq.select(root).where(cb.or(p1, p2));
        cq.orderBy(cb.asc(root.get("maTohop")));
        return em().createQuery(cq).getResultList();
    }

    public List<ToHop> findNangKhieuToHop() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ToHop> cq = cb.createQuery(ToHop.class);
        Root<ToHop> root = cq.from(ToHop.class);
        cq.select(root).where(cb.like(root.get("maTohop"), "NK%"));
        cq.orderBy(cb.asc(root.get("maTohop")));
        return em().createQuery(cq).getResultList();
    }
}
