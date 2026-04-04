package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.IThiSinhThanhTichDao;
import com.tuyensinh.entity.ThiSinhThanhTich;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;

public class ThiSinhThanhTichDao extends BaseDao<ThiSinhThanhTich> implements IThiSinhThanhTichDao {

    @Override
    protected Class<ThiSinhThanhTich> getEntityClass() {
        return ThiSinhThanhTich.class;
    }

    @Override
    public List<ThiSinhThanhTich> findByThiSinhId(Integer thisinhId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinhThanhTich> cq = cb.createQuery(ThiSinhThanhTich.class);
        Root<ThiSinhThanhTich> root = cq.from(ThiSinhThanhTich.class);
        Join<ThiSinhThanhTich, ?> thiSinhJoin = root.join("thiSinh");

        cq.select(root)
                .where(cb.equal(thiSinhJoin.get("thisinhId"), thisinhId))
                .orderBy(cb.desc(root.get("namDatGiai")), cb.asc(root.get("thanhtichId")));

        return em().createQuery(cq).getResultList();
    }

    @Override
    public List<ThiSinhThanhTich> findHopLeByThiSinhId(Integer thisinhId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinhThanhTich> cq = cb.createQuery(ThiSinhThanhTich.class);
        Root<ThiSinhThanhTich> root = cq.from(ThiSinhThanhTich.class);
        Join<ThiSinhThanhTich, ?> thiSinhJoin = root.join("thiSinh");

        cq.select(root)
                .where(
                        cb.and(
                                cb.equal(thiSinhJoin.get("thisinhId"), thisinhId),
                                cb.isTrue(root.get("isHopLe"))
                        )
                )
                .orderBy(cb.desc(root.get("namDatGiai")), cb.asc(root.get("thanhtichId")));

        return em().createQuery(cq).getResultList();
    }

    @Override
    public List<ThiSinhThanhTich> findByNhomThanhTich(String nhomThanhTich) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinhThanhTich> cq = cb.createQuery(ThiSinhThanhTich.class);
        Root<ThiSinhThanhTich> root = cq.from(ThiSinhThanhTich.class);

        cq.select(root)
                .where(cb.equal(root.get("nhomThanhTich"), nhomThanhTich))
                .orderBy(cb.desc(root.get("namDatGiai")), cb.asc(root.get("thanhtichId")));

        return em().createQuery(cq).getResultList();
    }

    @Override
    public List<ThiSinhThanhTich> findByCapThanhTich(String capThanhTich) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinhThanhTich> cq = cb.createQuery(ThiSinhThanhTich.class);
        Root<ThiSinhThanhTich> root = cq.from(ThiSinhThanhTich.class);

        cq.select(root)
                .where(cb.equal(root.get("capThanhTich"), capThanhTich))
                .orderBy(cb.desc(root.get("namDatGiai")), cb.asc(root.get("thanhtichId")));

        return em().createQuery(cq).getResultList();
    }
}