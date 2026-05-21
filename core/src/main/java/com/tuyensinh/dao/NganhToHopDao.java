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

    public List<NganhToHop> findPage(int page, int pageSize) {
        return em().createQuery(
                        "select nt from NganhToHop nt " +
                                "left join nt.nganh n " +
                                "left join nt.toHop th " +
                                "order by n.maNganh, th.maTohop, nt.nganhTohopId",
                        NganhToHop.class)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public long countAll() {
        return count();
    }

    public List<NganhToHop> searchPage(String keyword, int page, int pageSize) {
        String kw = "%" + normalizeKeyword(keyword) + "%";
        return em().createQuery(
                        "select nt from NganhToHop nt " +
                                "left join nt.nganh n " +
                                "left join nt.toHop th " +
                                "where lower(coalesce(n.maNganh, '')) like :kw " +
                                "or lower(coalesce(n.tenNganh, '')) like :kw " +
                                "or lower(coalesce(th.maTohop, '')) like :kw " +
                                "or lower(coalesce(th.tenTohop, '')) like :kw " +
                                "order by n.maNganh, th.maTohop, nt.nganhTohopId",
                        NganhToHop.class)
                .setParameter("kw", kw)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public long countSearch(String keyword) {
        String kw = "%" + normalizeKeyword(keyword) + "%";
        return em().createQuery(
                        "select count(nt) from NganhToHop nt " +
                                "left join nt.nganh n " +
                                "left join nt.toHop th " +
                                "where lower(coalesce(n.maNganh, '')) like :kw " +
                                "or lower(coalesce(n.tenNganh, '')) like :kw " +
                                "or lower(coalesce(th.maTohop, '')) like :kw " +
                                "or lower(coalesce(th.tenTohop, '')) like :kw",
                        Long.class)
                .setParameter("kw", kw)
                .getSingleResult();
    }



    public List<NganhToHop> searchPageByField(String field, String keyword, int page, int pageSize) {
        javax.persistence.TypedQuery<NganhToHop> q = buildSearchByFieldQuery(field, keyword, false);
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
        String select = countOnly ? "select count(nt) " : "select nt ";
        String jpql = select + "from NganhToHop nt left join nt.nganh n left join nt.toHop th ";
        String where;
        boolean like = false;
        switch (f) {
            case "ID": where = "nt.nganhTohopId = :id"; break;
            case "MANGANH": where = "lower(coalesce(n.maNganh, '')) = :text"; break;
            case "MATOHOP": where = "lower(coalesce(th.maTohop, '')) = :text"; break;
            case "TENNGANH": where = "lower(coalesce(n.tenNganh, '')) like :kw"; like = true; break;
            case "TENTOHOP": where = "lower(coalesce(th.tenTohop, '')) like :kw"; like = true; break;
            default:
                where = "lower(coalesce(n.maNganh, '')) = :text or lower(coalesce(th.maTohop, '')) = :text " +
                        "or lower(coalesce(n.tenNganh, '')) like :kw or lower(coalesce(th.tenTohop, '')) like :kw";
                like = true; break;
        }
        jpql += "where (" + where + ") ";
        if (!countOnly) jpql += "order by n.maNganh, th.maTohop, nt.nganhTohopId";
        javax.persistence.TypedQuery<R> q;

        if (countOnly) {

            q = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, Long.class);

        } else {

            q = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, NganhToHop.class);

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
