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

    /**
     * Nghiep vu tinh diem chi duoc lay chung chi da hop le va da xac minh.
     * Neu van lay CHUA_XAC_MINH thi cot xac minh tren giao dien gan nhu khong co tac dung.
     */
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
                                cb.isTrue(root.get("isHopLe")),
                                cb.equal(root.get("trangThaiXacMinh"), "DA_XAC_MINH")
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
                .orderBy(
                        cb.asc(thiSinhJoin.get("ho")),
                        cb.asc(thiSinhJoin.get("ten")),
                        cb.asc(root.get("chungchiId"))
                );

        return em().createQuery(cq).getResultList();
    }

    public List<ThiSinhChungChi> findPage(int page, int pageSize) {
        return em().createQuery(
                        "select cc from ThiSinhChungChi cc " +
                                "left join fetch cc.thiSinh ts " +
                                "order by cc.chungchiId",
                        ThiSinhChungChi.class)
                .setFirstResult((Math.max(1, page) - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public long countAll() {
        return count();
    }

    public List<ThiSinhChungChi> searchPage(String keyword, int page, int pageSize) {
        String kw = "%" + normalizeKeyword(keyword) + "%";
        return em().createQuery(
                        "select cc from ThiSinhChungChi cc " +
                                "left join fetch cc.thiSinh ts " +
                                "where lower(coalesce(ts.cccd, '')) like :kw " +
                                "or lower(coalesce(ts.sobaodanh, '')) like :kw " +
                                "or lower(coalesce(ts.ho, '')) like :kw " +
                                "or lower(coalesce(ts.ten, '')) like :kw " +
                                "or lower(coalesce(cc.loaiChungChi, '')) like :kw " +
                                "or lower(coalesce(cc.tenChungChi, '')) like :kw " +
                                "or lower(coalesce(cc.bacChungChi, '')) like :kw " +
                                "or lower(coalesce(cc.trangThaiXacMinh, '')) like :kw " +
                                "or lower(coalesce(cc.ghiChu, '')) like :kw " +
                                "order by cc.chungchiId",
                        ThiSinhChungChi.class)
                .setParameter("kw", kw)
                .setFirstResult((Math.max(1, page) - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public long countSearch(String keyword) {
        String kw = "%" + normalizeKeyword(keyword) + "%";
        return em().createQuery(
                        "select count(cc) from ThiSinhChungChi cc " +
                                "left join cc.thiSinh ts " +
                                "where lower(coalesce(ts.cccd, '')) like :kw " +
                                "or lower(coalesce(ts.sobaodanh, '')) like :kw " +
                                "or lower(coalesce(ts.ho, '')) like :kw " +
                                "or lower(coalesce(ts.ten, '')) like :kw " +
                                "or lower(coalesce(cc.loaiChungChi, '')) like :kw " +
                                "or lower(coalesce(cc.tenChungChi, '')) like :kw " +
                                "or lower(coalesce(cc.bacChungChi, '')) like :kw " +
                                "or lower(coalesce(cc.trangThaiXacMinh, '')) like :kw " +
                                "or lower(coalesce(cc.ghiChu, '')) like :kw",
                        Long.class)
                .setParameter("kw", kw)
                .getSingleResult();
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase();
    }
}
