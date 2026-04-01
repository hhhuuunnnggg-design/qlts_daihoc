package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.INganhPhuongThucDao;
import com.tuyensinh.entity.NganhPhuongThuc;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class NganhPhuongThucDao extends BaseDao<NganhPhuongThuc> implements INganhPhuongThucDao {

    @Override
    protected Class<NganhPhuongThuc> getEntityClass() {
        return NganhPhuongThuc.class;
    }

    public List<NganhPhuongThuc> findByNganhId(Integer nganhId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NganhPhuongThuc> cq = cb.createQuery(NganhPhuongThuc.class);
        Root<NganhPhuongThuc> root = cq.from(NganhPhuongThuc.class);
        Join<NganhPhuongThuc, ?> nganh = root.join("nganh");
        Join<NganhPhuongThuc, ?> phuongThuc = root.join("phuongThuc");
        cq.select(root).where(cb.equal(nganh.get("nganhId"), nganhId));
        cq.orderBy(cb.asc(phuongThuc.get("phuongthucId")));
        return em().createQuery(cq).getResultList();
    }

    public List<NganhPhuongThuc> findByPhuongThucId(Short phuongthucId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NganhPhuongThuc> cq = cb.createQuery(NganhPhuongThuc.class);
        Root<NganhPhuongThuc> root = cq.from(NganhPhuongThuc.class);
        Join<NganhPhuongThuc, ?> phuongThuc = root.join("phuongThuc");
        Join<NganhPhuongThuc, ?> nganh = root.join("nganh");
        cq.select(root).where(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));
        cq.orderBy(cb.asc(nganh.get("maNganh")));
        return em().createQuery(cq).getResultList();
    }

    public NganhPhuongThuc findByNganhAndPhuongThuc(Integer nganhId, Short phuongthucId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NganhPhuongThuc> cq = cb.createQuery(NganhPhuongThuc.class);
        Root<NganhPhuongThuc> root = cq.from(NganhPhuongThuc.class);
        Join<NganhPhuongThuc, ?> nganh = root.join("nganh");
        Join<NganhPhuongThuc, ?> phuongThuc = root.join("phuongThuc");
        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(nganh.get("nganhId"), nganhId));
        preds.add(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));
        cq.select(root).where(preds.toArray(new Predicate[0]));
        List<NganhPhuongThuc> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public List<NganhPhuongThuc> findAll() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NganhPhuongThuc> cq = cb.createQuery(NganhPhuongThuc.class);
        Root<NganhPhuongThuc> root = cq.from(NganhPhuongThuc.class);
        Join<NganhPhuongThuc, ?> nganh = root.join("nganh");
        Join<NganhPhuongThuc, ?> phuongThuc = root.join("phuongThuc");
        cq.select(root).orderBy(cb.asc(nganh.get("maNganh")), cb.asc(phuongThuc.get("phuongthucId")));
        return em().createQuery(cq).getResultList();
    }
}
