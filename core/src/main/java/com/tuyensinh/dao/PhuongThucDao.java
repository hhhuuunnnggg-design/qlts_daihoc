package com.tuyensinh.dao;

import com.tuyensinh.entity.PhuongThuc;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

public class PhuongThucDao extends BaseDao<PhuongThuc> {

    @Override
    protected Class<PhuongThuc> getEntityClass() {
        return PhuongThuc.class;
    }

    public Optional<PhuongThuc> findByMa(String maPhuongthuc) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<PhuongThuc> cq = cb.createQuery(PhuongThuc.class);
        Root<PhuongThuc> root = cq.from(PhuongThuc.class);
        cq.select(root).where(cb.equal(root.get("maPhuongthuc"), maPhuongthuc));
        List<PhuongThuc> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<PhuongThuc> findActive() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<PhuongThuc> cq = cb.createQuery(PhuongThuc.class);
        Root<PhuongThuc> root = cq.from(PhuongThuc.class);
        cq.select(root).where(cb.equal(root.get("isActive"), true));
        cq.orderBy(cb.asc(root.get("phuongthucId")));
        return em().createQuery(cq).getResultList();
    }

    public List<PhuongThuc> findAll() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<PhuongThuc> cq = cb.createQuery(PhuongThuc.class);
        Root<PhuongThuc> root = cq.from(PhuongThuc.class);
        cq.select(root).orderBy(cb.asc(root.get("phuongthucId")));
        return em().createQuery(cq).getResultList();
    }
}
