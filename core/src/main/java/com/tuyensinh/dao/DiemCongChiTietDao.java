package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.IDiemCongChiTietDao;
import com.tuyensinh.entity.DiemCongChiTiet;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class DiemCongChiTietDao extends BaseDao<DiemCongChiTiet> implements IDiemCongChiTietDao {

    @Override
    protected Class<DiemCongChiTiet> getEntityClass() {
        return DiemCongChiTiet.class;
    }

    @Override
    public List<DiemCongChiTiet> findByDiemCongId(Integer diemcongId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<DiemCongChiTiet> cq = cb.createQuery(DiemCongChiTiet.class);
        Root<DiemCongChiTiet> root = cq.from(DiemCongChiTiet.class);
        Join<DiemCongChiTiet, ?> diemCongJoin = root.join("diemCong");

        cq.select(root)
                .where(cb.equal(diemCongJoin.get("diemcongId"), diemcongId))
                .orderBy(
                        cb.asc(root.get("thuTuUuTien")),
                        cb.asc(root.get("diemcongCtId"))
                );

        return em().createQuery(cq).getResultList();
    }

    @Override
    public List<DiemCongChiTiet> findAppliedByDiemCongId(Integer diemcongId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<DiemCongChiTiet> cq = cb.createQuery(DiemCongChiTiet.class);
        Root<DiemCongChiTiet> root = cq.from(DiemCongChiTiet.class);
        Join<DiemCongChiTiet, ?> diemCongJoin = root.join("diemCong");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(diemCongJoin.get("diemcongId"), diemcongId));
        predicates.add(cb.isTrue(root.get("isApDung")));

        cq.select(root)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(
                        cb.asc(root.get("thuTuUuTien")),
                        cb.asc(root.get("diemcongCtId"))
                );

        return em().createQuery(cq).getResultList();
    }

    @Override
    public void deleteByDiemCongId(Integer diemcongId) {
        EntityManager em = em();
        em.getTransaction().begin();
        try {
            em.createQuery("DELETE FROM DiemCongChiTiet ct WHERE ct.diemCong.diemcongId = :id")
                    .setParameter("id", diemcongId)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }
}