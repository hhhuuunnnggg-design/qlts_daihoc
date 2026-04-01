package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.INganhToHopDao;
import com.tuyensinh.entity.NganhToHop;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

public class NganhToHopDao extends BaseDao<NganhToHop> implements INganhToHopDao {

    @Override
    protected Class<NganhToHop> getEntityClass() {
        return NganhToHop.class;
    }

    public List<NganhToHop> findByNganhId(Integer nganhId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NganhToHop> cq = cb.createQuery(NganhToHop.class);
        Root<NganhToHop> root = cq.from(NganhToHop.class);
        Join<NganhToHop, ?> nganh = root.join("nganh");
        Join<NganhToHop, ?> toHop = root.join("toHop");
        cq.select(root).where(cb.equal(nganh.get("nganhId"), nganhId));
        cq.orderBy(cb.asc(toHop.get("maTohop")));
        return em().createQuery(cq).getResultList();
    }

    public List<NganhToHop> findByToHopId(Integer tohopId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NganhToHop> cq = cb.createQuery(NganhToHop.class);
        Root<NganhToHop> root = cq.from(NganhToHop.class);
        Join<NganhToHop, ?> toHop = root.join("toHop");
        Join<NganhToHop, ?> nganh = root.join("nganh");
        cq.select(root).where(cb.equal(toHop.get("tohopId"), tohopId));
        cq.orderBy(cb.asc(nganh.get("maNganh")));
        return em().createQuery(cq).getResultList();
    }

    public Optional<NganhToHop> findByNganhAndToHop(Integer nganhId, Integer tohopId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NganhToHop> cq = cb.createQuery(NganhToHop.class);
        Root<NganhToHop> root = cq.from(NganhToHop.class);
        Join<NganhToHop, ?> nganh = root.join("nganh");
        Join<NganhToHop, ?> toHop = root.join("toHop");
        cq.select(root).where(
            cb.and(
                cb.equal(nganh.get("nganhId"), nganhId),
                cb.equal(toHop.get("tohopId"), tohopId)
            )
        );
        List<NganhToHop> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<NganhToHop> findAll() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NganhToHop> cq = cb.createQuery(NganhToHop.class);
        Root<NganhToHop> root = cq.from(NganhToHop.class);
        Join<NganhToHop, ?> nganh = root.join("nganh");
        Join<NganhToHop, ?> toHop = root.join("toHop");
        cq.select(root).orderBy(cb.asc(nganh.get("maNganh")), cb.asc(toHop.get("maTohop")));
        return em().createQuery(cq).getResultList();
    }
}
