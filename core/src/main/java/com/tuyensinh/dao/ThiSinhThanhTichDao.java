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
                                cb.isTrue(root.get("isHopLe")),
                                cb.equal(root.get("trangThaiXacMinh"), "DA_XAC_MINH")
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

    public List<ThiSinhThanhTich> findPage(int page, int pageSize) {
        return em().createQuery(
                        "select tt from ThiSinhThanhTich tt " +
                                "left join tt.thiSinh ts " +
                                "order by tt.thanhtichId",
                        ThiSinhThanhTich.class)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public long countAll() {
        return count();
    }

    public List<ThiSinhThanhTich> searchPage(String keyword, int page, int pageSize) {
        String kw = "%" + normalizeKeyword(keyword) + "%";
        return em().createQuery(
                        "select tt from ThiSinhThanhTich tt " +
                                "left join tt.thiSinh ts " +
                                "where lower(coalesce(ts.cccd, '')) like :kw " +
                                "or lower(coalesce(ts.sobaodanh, '')) like :kw " +
                                "or lower(coalesce(ts.ho, '')) like :kw " +
                                "or lower(coalesce(ts.ten, '')) like :kw " +
                                "or lower(coalesce(tt.nhomThanhTich, '')) like :kw " +
                                "or lower(coalesce(tt.capThanhTich, '')) like :kw " +
                                "or lower(coalesce(tt.loaiGiai, '')) like :kw " +
                                "or lower(coalesce(tt.tenThanhTich, '')) like :kw " +
                                "or lower(coalesce(tt.monDatGiai, '')) like :kw " +
                                "or lower(coalesce(tt.linhVuc, '')) like :kw " +
                                "or lower(coalesce(tt.trangThaiXacMinh, '')) like :kw " +
                                "or lower(coalesce(tt.ghiChu, '')) like :kw " +
                                "order by tt.thanhtichId",
                        ThiSinhThanhTich.class)
                .setParameter("kw", kw)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public long countSearch(String keyword) {
        String kw = "%" + normalizeKeyword(keyword) + "%";
        return em().createQuery(
                        "select count(tt) from ThiSinhThanhTich tt " +
                                "left join tt.thiSinh ts " +
                                "where lower(coalesce(ts.cccd, '')) like :kw " +
                                "or lower(coalesce(ts.sobaodanh, '')) like :kw " +
                                "or lower(coalesce(ts.ho, '')) like :kw " +
                                "or lower(coalesce(ts.ten, '')) like :kw " +
                                "or lower(coalesce(tt.nhomThanhTich, '')) like :kw " +
                                "or lower(coalesce(tt.capThanhTich, '')) like :kw " +
                                "or lower(coalesce(tt.loaiGiai, '')) like :kw " +
                                "or lower(coalesce(tt.tenThanhTich, '')) like :kw " +
                                "or lower(coalesce(tt.monDatGiai, '')) like :kw " +
                                "or lower(coalesce(tt.linhVuc, '')) like :kw " +
                                "or lower(coalesce(tt.trangThaiXacMinh, '')) like :kw " +
                                "or lower(coalesce(tt.ghiChu, '')) like :kw",
                        Long.class)
                .setParameter("kw", kw)
                .getSingleResult();
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase();
    }

}