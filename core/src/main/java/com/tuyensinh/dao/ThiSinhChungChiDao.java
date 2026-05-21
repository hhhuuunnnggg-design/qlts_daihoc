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



    public List<ThiSinhChungChi> searchPageByField(String field, String keyword, int page, int pageSize) {
        javax.persistence.TypedQuery<ThiSinhChungChi> q = buildSearchByFieldQuery(field, keyword, false);
        q.setFirstResult((Math.max(1, page) - 1) * pageSize);
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    public long countSearchByField(String field, String keyword) {
        javax.persistence.TypedQuery<Long> q = buildSearchByFieldQuery(field, keyword, true);
        return q.getSingleResult();
    }

    @SuppressWarnings("unchecked")
    private <R> javax.persistence.TypedQuery<R> buildSearchByFieldQuery(String field, String keyword, boolean countOnly) {
        String f = field == null ? "ALL" : field.trim().toUpperCase();
        String raw = keyword == null ? "" : keyword.trim();
        String low = raw.toLowerCase();
        String select = countOnly ? "select count(cc) " : "select cc ";
        String fetch = countOnly ? "" : "fetch ";
        String jpql = select + "from ThiSinhChungChi cc left join " + fetch + "cc.thiSinh ts ";
        String where;
        boolean like = false;
        switch (f) {
            case "ID": where = "cc.chungchiId = :id"; break;
            case "CCCD": where = "lower(coalesce(ts.cccd, '')) = :text"; break;
            case "SBD": where = "lower(coalesce(ts.sobaodanh, '')) = :text"; break;
            case "LOAICC": where = "lower(coalesce(cc.loaiChungChi, '')) = :text"; break;
            case "BAC": where = "lower(coalesce(cc.bacChungChi, '')) = :text"; break;
            case "XACMINH": where = "lower(coalesce(cc.trangThaiXacMinh, '')) = :text"; break;
            case "HOTEN": where = "lower(concat(concat(coalesce(ts.ho, ''), ' '), coalesce(ts.ten, ''))) like :kw"; like = true; break;
            case "TENCC": where = "lower(coalesce(cc.tenChungChi, '')) like :kw"; like = true; break;
            default:
                where = "lower(coalesce(ts.cccd, '')) = :text or lower(coalesce(ts.sobaodanh, '')) = :text " +
                        "or lower(coalesce(cc.loaiChungChi, '')) = :text or lower(coalesce(cc.bacChungChi, '')) = :text " +
                        "or lower(coalesce(cc.trangThaiXacMinh, '')) = :text " +
                        "or lower(concat(concat(coalesce(ts.ho, ''), ' '), coalesce(ts.ten, ''))) like :kw " +
                        "or lower(coalesce(cc.tenChungChi, '')) like :kw or lower(coalesce(cc.ghiChu, '')) like :kw";
                like = true; break;
        }
        jpql += "where (" + where + ") ";
        if (!countOnly) jpql += "order by cc.chungchiId";
        javax.persistence.TypedQuery<R> q;

        if (countOnly) {

            q = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, Long.class);

        } else {

            q = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, ThiSinhChungChi.class);

        }
        if (where.contains(":id")) q.setParameter("id", parseIntegerOrNeverMatch(raw));
        if (where.contains(":text")) q.setParameter("text", low);
        if (like || where.contains(":kw")) q.setParameter("kw", "%" + low + "%");
        return q;
    }

    private Integer parseIntegerOrNeverMatch(String value) {
        try { return Integer.parseInt(value.trim()); } catch (Exception e) { return -1; }
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase();
    }
}
