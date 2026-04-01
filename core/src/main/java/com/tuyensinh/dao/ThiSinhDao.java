package com.tuyensinh.dao;

import com.tuyensinh.entity.ThiSinh;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ThiSinhDao extends BaseDao<ThiSinh> {

    @Override
    protected Class<ThiSinh> getEntityClass() {
        return ThiSinh.class;
    }

    public Optional<ThiSinh> findByCccd(String cccd) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinh> cq = cb.createQuery(ThiSinh.class);
        Root<ThiSinh> root = cq.from(ThiSinh.class);
        cq.select(root).where(cb.equal(root.get("cccd"), cccd));
        List<ThiSinh> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<ThiSinh> findBySoBaoDanh(String sobaodanh) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinh> cq = cb.createQuery(ThiSinh.class);
        Root<ThiSinh> root = cq.from(ThiSinh.class);
        cq.select(root).where(cb.equal(root.get("sobaodanh"), sobaodanh));
        List<ThiSinh> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<ThiSinh> searchByCccdOrHoTen(String keyword) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinh> cq = cb.createQuery(ThiSinh.class);
        Root<ThiSinh> root = cq.from(ThiSinh.class);
        String kw = "%" + keyword + "%";
        Predicate p1 = cb.like(root.get("cccd"), kw);
        Predicate p2 = cb.like(root.get("ho"), kw);
        Predicate p3 = cb.like(root.get("ten"), kw);
        cq.select(root).where(cb.or(p1, p2, p3));
        cq.orderBy(cb.asc(root.get("ten")), cb.asc(root.get("ho")));
        return em().createQuery(cq).getResultList();
    }

    public List<ThiSinh> findByPage(int page, int pageSize) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinh> cq = cb.createQuery(ThiSinh.class);
        Root<ThiSinh> root = cq.from(ThiSinh.class);
        cq.select(root).orderBy(cb.asc(root.get("ten")), cb.asc(root.get("ho")));
        TypedQuery<ThiSinh> q = em().createQuery(cq);
        q.setFirstResult((page - 1) * pageSize);
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    public List<ThiSinh> findByPageWithSearch(String keyword, int page, int pageSize) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinh> cq = cb.createQuery(ThiSinh.class);
        Root<ThiSinh> root = cq.from(ThiSinh.class);
        String kw = "%" + keyword + "%";
        Predicate p1 = cb.like(root.get("cccd"), kw);
        Predicate p2 = cb.like(root.get("ho"), kw);
        Predicate p3 = cb.like(root.get("ten"), kw);
        Predicate p4 = cb.like(root.get("sobaodanh"), kw);
        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.or(p1, p2, p3, p4));
        cq.select(root).where(preds.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("ten")), cb.asc(root.get("ho")));
        TypedQuery<ThiSinh> q = em().createQuery(cq);
        q.setFirstResult((page - 1) * pageSize);
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    public long countBySearch(String keyword) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ThiSinh> root = cq.from(ThiSinh.class);
        cq.select(cb.count(root));
        if (keyword == null || keyword.trim().isEmpty()) {
            return em().createQuery(cq).getSingleResult();
        }
        String kw = "%" + keyword + "%";
        Predicate p1 = cb.like(root.get("cccd"), kw);
        Predicate p2 = cb.like(root.get("ho"), kw);
        Predicate p3 = cb.like(root.get("ten"), kw);
        cq.where(cb.or(p1, p2, p3));
        return em().createQuery(cq).getSingleResult();
    }

    public List<ThiSinh> findByNguoiDungId(Integer nguoidungId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinh> cq = cb.createQuery(ThiSinh.class);
        Root<ThiSinh> root = cq.from(ThiSinh.class);
        Join<ThiSinh, ?> nguoiDung = root.join("nguoiDung");
        cq.select(root).where(cb.equal(nguoiDung.get("nguoidungId"), nguoidungId));
        return em().createQuery(cq).getResultList();
    }

    public String generateSoBaoDanh() {
        String hql = "SELECT MAX(ts.sobaodanh) FROM ThiSinh ts WHERE ts.sobaodanh IS NOT NULL";
        javax.persistence.TypedQuery<String> q = em().createQuery(hql, String.class);
        String maxSo = q.getSingleResult();
        if (maxSo == null) {
            return "TS0001";
        }
        int num = Integer.parseInt(maxSo.replace("TS", ""));
        return String.format("TS%04d", num + 1);
    }
}
