package com.tuyensinh.dao;

import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Optional;

public abstract class BaseDao<T> {

    protected abstract Class<T> getEntityClass();

    protected Session getSession() {
        return HibernateUtil.getSession();
    }

    public T findById(Integer id) {
        try (Session session = getSession()) {
            return session.get(getEntityClass(), id);
        }
    }

    public Optional<T> findByIdOptional(Integer id) {
        return Optional.ofNullable(findById(id));
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        try (Session session = getSession()) {
            Query<T> query = session.createQuery("FROM " + getEntityClass().getSimpleName(), getEntityClass());
            return query.getResultList();
        }
    }

    public T save(T entity) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.persist(entity);
            session.getTransaction().commit();
            return entity;
        }
    }

    public void update(T entity) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.update(entity);
            session.getTransaction().commit();
        }
    }

    public void saveOrUpdate(T entity) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.saveOrUpdate(entity);
            session.getTransaction().commit();
        }
    }

    public void delete(T entity) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.delete(entity);
            session.getTransaction().commit();
        }
    }

    public void deleteById(Integer id) {
        T entity = findById(id);
        if (entity != null) {
            delete(entity);
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> findByPage(int page, int pageSize) {
        try (Session session = getSession()) {
            Query<T> query = session.createQuery("FROM " + getEntityClass().getSimpleName(), getEntityClass());
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        }
    }

    public long count() {
        try (Session session = getSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(*) FROM " + getEntityClass().getSimpleName(), Long.class);
            return query.getSingleResult();
        }
    }

    @SuppressWarnings("unchecked")
    protected List<T> executeQuery(String hql, List<Object> params) {
        try (Session session = getSession()) {
            Query<T> query = session.createQuery(hql, getEntityClass());
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    query.setParameter(i, params.get(i));
                }
            }
            return query.getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    protected T executeSingleResult(String hql, List<Object> params) {
        try (Session session = getSession()) {
            Query<T> query = session.createQuery(hql, getEntityClass());
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    query.setParameter(i, params.get(i));
                }
            }
            return query.setMaxResults(1).uniqueResult();
        }
    }
}
