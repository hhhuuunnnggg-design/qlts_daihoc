package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.INganhToHopMonDao;
import com.tuyensinh.entity.NganhToHopMon;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NganhToHopMonDao extends BaseDao<NganhToHopMon> implements INganhToHopMonDao {

    @Override
    protected Class<NganhToHopMon> getEntityClass() {
        return NganhToHopMon.class;
    }

    @Override
    public List<NganhToHopMon> findByNganhToHopId(Integer nganhToHopId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NganhToHopMon> cq = cb.createQuery(NganhToHopMon.class);
        Root<NganhToHopMon> root = cq.from(NganhToHopMon.class);
        Join<NganhToHopMon, ?> nganhToHop = root.join("nganhToHop");
        Join<NganhToHopMon, ?> mon = root.join("mon");
        cq.select(root).where(cb.equal(nganhToHop.get("nganhTohopId"), nganhToHopId));
        cq.orderBy(cb.asc(mon.get("maMon")));
        return em().createQuery(cq).getResultList();
    }

    @Override
    public Optional<NganhToHopMon> findByNganhToHopAndMon(Integer nganhToHopId, Integer monId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NganhToHopMon> cq = cb.createQuery(NganhToHopMon.class);
        Root<NganhToHopMon> root = cq.from(NganhToHopMon.class);
        Join<NganhToHopMon, ?> nganhToHop = root.join("nganhToHop");
        Join<NganhToHopMon, ?> mon = root.join("mon");
        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(nganhToHop.get("nganhTohopId"), nganhToHopId));
        preds.add(cb.equal(mon.get("monId"), monId));
        cq.select(root).where(preds.toArray(new Predicate[0]));
        List<NganhToHopMon> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<NganhToHopMon> findAll() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NganhToHopMon> cq = cb.createQuery(NganhToHopMon.class);
        Root<NganhToHopMon> root = cq.from(NganhToHopMon.class);
        Join<NganhToHopMon, ?> nganhToHop = root.join("nganhToHop");
        Join<NganhToHopMon, ?> nganh = nganhToHop.join("nganh");
        Join<NganhToHopMon, ?> toHop = nganhToHop.join("toHop");
        Join<NganhToHopMon, ?> mon = root.join("mon");
        cq.select(root).orderBy(cb.asc(nganh.get("maNganh")), cb.asc(toHop.get("maTohop")), cb.asc(mon.get("maMon")));
        return em().createQuery(cq).getResultList();
    }

    @Override
    public long countAll() {
        return count();
    }
}
