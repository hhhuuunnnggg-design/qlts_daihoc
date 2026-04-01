package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.IBangQuyDoiDao;
import com.tuyensinh.entity.BangQuyDoi;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BangQuyDoiDao extends BaseDao<BangQuyDoi> implements IBangQuyDoiDao {

    @Override
    protected Class<BangQuyDoi> getEntityClass() {
        return BangQuyDoi.class;
    }

    public Optional<BangQuyDoi> findByMa(String maQuydoi) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<BangQuyDoi> cq = cb.createQuery(BangQuyDoi.class);
        Root<BangQuyDoi> root = cq.from(BangQuyDoi.class);
        cq.select(root).where(cb.equal(root.get("maQuydoi"), maQuydoi));
        List<BangQuyDoi> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<BangQuyDoi> findByPhuongThuc(Short phuongthucId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<BangQuyDoi> cq = cb.createQuery(BangQuyDoi.class);
        Root<BangQuyDoi> root = cq.from(BangQuyDoi.class);
        Join<BangQuyDoi, ?> phuongThuc = root.join("phuongThuc");
        cq.select(root).where(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));
        cq.orderBy(cb.asc(root.get("diemTu")));
        return em().createQuery(cq).getResultList();
    }

    public List<BangQuyDoi> search(String keyword) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<BangQuyDoi> cq = cb.createQuery(BangQuyDoi.class);
        Root<BangQuyDoi> root = cq.from(BangQuyDoi.class);
        Join<BangQuyDoi, ?> phuongThuc = root.join("phuongThuc");
        String kw = "%" + keyword + "%";
        Predicate p1 = cb.like(root.get("maQuydoi"), kw);
        Predicate p2 = cb.like(phuongThuc.get("maPhuongthuc"), kw);
        cq.select(root).where(cb.or(p1, p2));
        Join<BangQuyDoi, ?> pt2 = root.join("phuongThuc");
        cq.orderBy(cb.asc(pt2.get("phuongthucId")), cb.asc(root.get("diemTu")));
        return em().createQuery(cq).getResultList();
    }

    public List<BangQuyDoi> findAll() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<BangQuyDoi> cq = cb.createQuery(BangQuyDoi.class);
        Root<BangQuyDoi> root = cq.from(BangQuyDoi.class);
        Join<BangQuyDoi, ?> phuongThuc = root.join("phuongThuc");
        cq.select(root).orderBy(cb.asc(phuongThuc.get("phuongthucId")), cb.asc(root.get("diemTu")));
        return em().createQuery(cq).getResultList();
    }

    public BangQuyDoi quyDoiDiem(Short phuongthucId, Integer tohopId, Integer monId, BigDecimal diemGoc) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<BangQuyDoi> cq = cb.createQuery(BangQuyDoi.class);
        Root<BangQuyDoi> root = cq.from(BangQuyDoi.class);
        Join<BangQuyDoi, ?> phuongThuc = root.join("phuongThuc");
        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));
        preds.add(cb.le(root.get("diemTu"), diemGoc));
        preds.add(cb.ge(root.get("diemDen"), diemGoc));
        if (tohopId != null) {
            Join<BangQuyDoi, ?> toHop = root.join("toHop");
            preds.add(cb.equal(toHop.get("tohopId"), tohopId));
        }
        if (monId != null) {
            Join<BangQuyDoi, ?> mon = root.join("mon");
            preds.add(cb.equal(mon.get("monId"), monId));
        }
        cq.select(root).where(preds.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("diemTu")));
        List<BangQuyDoi> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }
}
