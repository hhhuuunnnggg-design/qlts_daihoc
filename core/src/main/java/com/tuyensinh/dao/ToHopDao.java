package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.IToHopDao;
import com.tuyensinh.entity.ToHop;
import com.tuyensinh.entity.ToHopMon;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

public class ToHopDao extends BaseDao<ToHop> implements IToHopDao {

    @Override
    protected Class<ToHop> getEntityClass() {
        return ToHop.class;
    }

    public Optional<ToHop> findByMa(String maTohop) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ToHop> cq = cb.createQuery(ToHop.class);
        Root<ToHop> root = cq.from(ToHop.class);
        cq.select(root).where(cb.equal(root.get("maTohop"), maTohop));
        List<ToHop> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<ToHop> findAll() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ToHop> cq = cb.createQuery(ToHop.class);
        Root<ToHop> root = cq.from(ToHop.class);
        cq.select(root).orderBy(cb.asc(root.get("maTohop")));
        return em().createQuery(cq).getResultList();
    }

    public List<ToHopMon> findMonByToHopId(Integer tohopId) {
        // Fetch luon mon de TinhDiemService doc thm.getMon() ngoai query khong bi LazyInitializationException.
        return em().createQuery(
                        "select thm " +
                                "from ToHopMon thm " +
                                "join fetch thm.toHop th " +
                                "join fetch thm.mon m " +
                                "where th.tohopId = :tohopId " +
                                "order by thm.thuTu", ToHopMon.class)
                .setParameter("tohopId", tohopId)
                .getResultList();
    }

    public void saveToHopMon(ToHopMon entity) {
        EntityManager em = em();
        em.getTransaction().begin();
        try {
            em.unwrap(org.hibernate.Session.class).saveOrUpdate(entity);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void deleteToHopMon(ToHopMon entity) {
        EntityManager em = em();
        em.getTransaction().begin();
        try {
            em.remove(em.contains(entity) ? entity : em.merge(entity));
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public List<ToHop> searchByMaOrTen(String keyword) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ToHop> cq = cb.createQuery(ToHop.class);
        Root<ToHop> root = cq.from(ToHop.class);
        String kw = "%" + keyword + "%";
        Predicate p1 = cb.like(root.get("maTohop"), kw);
        Predicate p2 = cb.like(root.get("tenTohop"), kw);
        cq.select(root).where(cb.or(p1, p2));
        cq.orderBy(cb.asc(root.get("maTohop")));
        return em().createQuery(cq).getResultList();
    }

    public List<ToHop> findNangKhieuToHop() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ToHop> cq = cb.createQuery(ToHop.class);
        Root<ToHop> root = cq.from(ToHop.class);
        cq.select(root).where(cb.like(root.get("maTohop"), "NK%"));
        cq.orderBy(cb.asc(root.get("maTohop")));
        return em().createQuery(cq).getResultList();
    }

    public List<ToHop> findPage(int page, int pageSize) {
        return em().createQuery(
                        "select th from ToHop th order by th.maTohop, th.tohopId",
                        ToHop.class)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public long countAll() {
        return count();
    }

    public List<ToHop> searchByMaOrTenPage(String keyword, int page, int pageSize) {
        String kw = "%" + normalizeKeyword(keyword) + "%";
        return em().createQuery(
                        "select th from ToHop th " +
                                "where lower(coalesce(th.maTohop, '')) like :kw " +
                                "or lower(coalesce(th.tenTohop, '')) like :kw " +
                                "order by th.maTohop, th.tohopId",
                        ToHop.class)
                .setParameter("kw", kw)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public long countSearchByMaOrTen(String keyword) {
        String kw = "%" + normalizeKeyword(keyword) + "%";
        return em().createQuery(
                        "select count(th) from ToHop th " +
                                "where lower(coalesce(th.maTohop, '')) like :kw " +
                                "or lower(coalesce(th.tenTohop, '')) like :kw",
                        Long.class)
                .setParameter("kw", kw)
                .getSingleResult();
    }



    public List<ToHop> searchPageByField(String field, String keyword, int page, int pageSize) {
        javax.persistence.TypedQuery<ToHop> q = buildSearchByFieldQuery(field, keyword, false);
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
        String select = countOnly ? "select count(distinct th) " : "select distinct th ";
        String jpql = select + "from ToHop th left join th.danhSachToHopMon thm left join thm.mon m ";
        String where;
        boolean like = false;
        switch (f) {
            case "ID":
                where = "th.tohopId = :id";
                break;
            case "MATOHOP":
                where = "lower(coalesce(th.maTohop, '')) = :text";
                break;
            case "MAMON":
                where = "lower(coalesce(m.maMon, '')) = :text";
                break;
            case "TENTOHOP":
                where = "lower(coalesce(th.tenTohop, '')) like :kw";
                like = true;
                break;
            default:
                where = "lower(coalesce(th.maTohop, '')) = :text " +
                        "or lower(coalesce(m.maMon, '')) = :text " +
                        "or lower(coalesce(th.tenTohop, '')) like :kw";
                like = true;
                break;
        }
        jpql += "where (" + where + ") ";
        if (!countOnly) jpql += "order by th.maTohop, th.tohopId";
        javax.persistence.TypedQuery<R> q;

        if (countOnly) {

            q = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, Long.class);

        } else {

            q = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, ToHop.class);

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
