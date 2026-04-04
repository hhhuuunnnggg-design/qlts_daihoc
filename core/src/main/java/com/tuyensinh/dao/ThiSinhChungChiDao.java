package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.IThiSinhChungChiDao;
import com.tuyensinh.entity.ThiSinhChungChi;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;

public class ThiSinhChungChiDao extends BaseDao<ThiSinhChungChi> implements IThiSinhChungChiDao {

    @Override
    protected Class<ThiSinhChungChi> getEntityClass() {
        return ThiSinhChungChi.class;
    }

    @Override
    public List<ThiSinhChungChi> findByThiSinhId(Integer thisinhId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinhChungChi> cq = cb.createQuery(ThiSinhChungChi.class);
        Root<ThiSinhChungChi> root = cq.from(ThiSinhChungChi.class);
        Join<ThiSinhChungChi, ?> thiSinhJoin = root.join("thiSinh");

        cq.select(root)
                .where(cb.equal(thiSinhJoin.get("thisinhId"), thisinhId))
                .orderBy(cb.desc(root.get("ngayCap")), cb.asc(root.get("chungchiId")));

        return em().createQuery(cq).getResultList();
    }

    @Override
    public List<ThiSinhChungChi> findHopLeByThiSinhId(Integer thisinhId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinhChungChi> cq = cb.createQuery(ThiSinhChungChi.class);
        Root<ThiSinhChungChi> root = cq.from(ThiSinhChungChi.class);
        Join<ThiSinhChungChi, ?> thiSinhJoin = root.join("thiSinh");

        cq.select(root)
                .where(
                        cb.and(
                                cb.equal(thiSinhJoin.get("thisinhId"), thisinhId),
                                cb.isTrue(root.get("isHopLe"))
                        )
                )
                .orderBy(cb.desc(root.get("ngayCap")), cb.asc(root.get("chungchiId")));

        return em().createQuery(cq).getResultList();
    }

    @Override
    public List<ThiSinhChungChi> findByLoaiChungChi(String loaiChungChi) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinhChungChi> cq = cb.createQuery(ThiSinhChungChi.class);
        Root<ThiSinhChungChi> root = cq.from(ThiSinhChungChi.class);
        Join<ThiSinhChungChi, ?> thiSinhJoin = root.join("thiSinh");

        cq.select(root)
                .where(cb.equal(root.get("loaiChungChi"), loaiChungChi))
                .orderBy(cb.asc(thiSinhJoin.get("ten")), cb.asc(thiSinhJoin.get("ho")));

        return em().createQuery(cq).getResultList();
    }
}