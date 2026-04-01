package com.tuyensinh.dao;

import com.tuyensinh.util.HibernateUtil;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDao<T> {

    protected abstract Class<T> getEntityClass();

    private static final ThreadLocal<EntityManager> EM = new ThreadLocal<>();

    protected EntityManager em() {
        EntityManager current = EM.get();
        if (current == null || !current.isOpen()) {
            current = HibernateUtil.getSessionFactory().createEntityManager();
            EM.set(current);
        }
        return current;
    }

    protected CriteriaBuilder cb() {
        return em().getCriteriaBuilder();
    }

    public T findById(Integer id) {
        return em().find(getEntityClass(), id);
    }

    public T save(T entity) {
        EntityManager em = em();
        em.getTransaction().begin();
        try {
            em.persist(entity);
            em.getTransaction().commit();
            return entity;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void update(T entity) {
        EntityManager em = em();
        em.getTransaction().begin();
        try {
            em.merge(entity);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void saveOrUpdate(T entity) {
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

    public void delete(T entity) {
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

    public void deleteById(Integer id) {
        T entity = findById(id);
        if (entity != null) {
            delete(entity);
        }
    }

    public List<T> findAll() {
        CriteriaQuery<T> cq = cb().createQuery(getEntityClass());
        cq.from(getEntityClass());
        return em().createQuery(cq).getResultList();
    }

    public List<T> findByPage(int page, int pageSize) {
        CriteriaQuery<T> cq = cb().createQuery(getEntityClass());
        cq.from(getEntityClass());
        TypedQuery<T> q = em().createQuery(cq);
        q.setFirstResult((page - 1) * pageSize);
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    public long count() {
        CriteriaQuery<Long> cq = cb().createQuery(Long.class);
        cq.select(cb().count(cq.from(getEntityClass())));
        return em().createQuery(cq).getSingleResult();
    }

    protected List<T> findWhere(List<Predicate> preds) {
        CriteriaQuery<T> cq = cb().createQuery(getEntityClass());
        Root<T> root = cq.from(getEntityClass());
        if (!preds.isEmpty()) {
            cq.where(preds.toArray(new Predicate[0]));
        }
        return em().createQuery(cq).getResultList();
    }

    protected TypedQuery<T> findWherePaginated(List<Predicate> preds, int page, int pageSize) {
        CriteriaQuery<T> cq = cb().createQuery(getEntityClass());
        Root<T> root = cq.from(getEntityClass());
        if (!preds.isEmpty()) {
            cq.where(preds.toArray(new Predicate[0]));
        }
        TypedQuery<T> q = em().createQuery(cq);
        q.setFirstResult((page - 1) * pageSize);
        q.setMaxResults(pageSize);
        return q;
    }

    protected long countWhere(List<Predicate> preds) {
        CriteriaQuery<Long> cq = cb().createQuery(Long.class);
        Root<T> root = cq.from(getEntityClass());
        cq.select(cb().count(root));
        if (!preds.isEmpty()) {
            cq.where(preds.toArray(new Predicate[0]));
        }
        return em().createQuery(cq).getSingleResult();
    }

    protected <Y> Subquery<Y> subquery(Class<Y> type) {
        return cb().createQuery(type).subquery(type);
    }

    public static void closeCurrentEm() {
        EntityManager em = EM.get();
        if (em != null && em.isOpen()) {
            em.close();
        }
        EM.remove();
    }
}
