package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.IThiSinhDao;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ThiSinhDao extends BaseDao<ThiSinh> implements IThiSinhDao {

    @Override
    protected Class<ThiSinh> getEntityClass() {
        return ThiSinh.class;
    }

    public Optional<ThiSinh> findByCccd(String cccd) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinh> cq = cb.createQuery(ThiSinh.class);
        Root<ThiSinh> root = cq.from(ThiSinh.class);
        cq.select(root).where(cb.equal(root.get("cccd"), cccd));
        List<ThiSinh> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<ThiSinh> findBySoBaoDanh(String sobaodanh) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinh> cq = cb.createQuery(ThiSinh.class);
        Root<ThiSinh> root = cq.from(ThiSinh.class);
        cq.select(root).where(cb.equal(root.get("sobaodanh"), sobaodanh));
        List<ThiSinh> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<ThiSinh> searchByCccdOrHoTen(String keyword) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinh> cq = cb.createQuery(ThiSinh.class);
        Root<ThiSinh> root = cq.from(ThiSinh.class);
        String kw = "%" + keyword + "%";
        Predicate p1 = cb.like(root.get("cccd"), kw);
        Predicate p2 = cb.like(root.get("ho"), kw);
        Predicate p3 = cb.like(root.get("ten"), kw);
        cq.select(root).where(cb.or(p1, p2, p3));
        cq.orderBy(cb.asc(root.get("ten")), cb.asc(root.get("ho")));
        return em().createQuery(cq).getResultList();
    }

    public List<ThiSinh> findByPage(int page, int pageSize) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinh> cq = cb.createQuery(ThiSinh.class);
        Root<ThiSinh> root = cq.from(ThiSinh.class);
        cq.select(root).orderBy(cb.asc(root.get("ten")), cb.asc(root.get("ho")));
        TypedQuery<ThiSinh> q = em().createQuery(cq);
        q.setFirstResult((page - 1) * pageSize);
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    public List<ThiSinh> findByPageWithSearch(String keyword, int page, int pageSize) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinh> cq = cb.createQuery(ThiSinh.class);
        Root<ThiSinh> root = cq.from(ThiSinh.class);
        String kw = "%" + keyword + "%";
        Predicate p1 = cb.like(root.get("cccd"), kw);
        Predicate p2 = cb.like(root.get("ho"), kw);
        Predicate p3 = cb.like(root.get("ten"), kw);
        Predicate p4 = cb.like(root.get("sobaodanh"), kw);
        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.or(p1, p2, p3, p4));
        cq.select(root).where(preds.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("ten")), cb.asc(root.get("ho")));
        TypedQuery<ThiSinh> q = em().createQuery(cq);
        q.setFirstResult((page - 1) * pageSize);
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    public long countBySearch(String keyword) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ThiSinh> root = cq.from(ThiSinh.class);
        cq.select(cb.count(root));
        if (keyword == null || keyword.trim().isEmpty()) {
            return em().createQuery(cq).getSingleResult();
        }
        String kw = "%" + keyword + "%";
        Predicate p1 = cb.like(root.get("cccd"), kw);
        Predicate p2 = cb.like(root.get("ho"), kw);
        Predicate p3 = cb.like(root.get("ten"), kw);
        Predicate p4 = cb.like(root.get("sobaodanh"), kw);
        cq.where(cb.or(p1, p2, p3, p4));
        return em().createQuery(cq).getSingleResult();
    }



    public List<ThiSinh> findByPageWithSearch(String field, String keyword, int page, int pageSize) {
        TypedQuery<ThiSinh> q = buildSearchByFieldQuery(field, keyword, false);
        q.setFirstResult((Math.max(1, page) - 1) * pageSize);
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    public long countBySearch(String field, String keyword) {
        TypedQuery<Long> q = buildSearchByFieldQuery(field, keyword, true);
        return q.getSingleResult();
    }

    @SuppressWarnings("unchecked")
    private <R> TypedQuery<R> buildSearchByFieldQuery(String field, String keyword, boolean countOnly) {
        String f = field == null ? "ALL" : field.trim().toUpperCase();
        String raw = keyword == null ? "" : keyword.trim();
        String low = raw.toLowerCase();

        String select = countOnly ? "select count(ts) " : "select ts ";
        String jpql = select + "from ThiSinh ts " +
                "left join ts.doiTuongUutien dt " +
                "left join ts.khuVucUutien kv ";
        String where;
        boolean like = false;
        switch (f) {
            case "ID":
                where = "ts.thisinhId = :id";
                break;
            case "CCCD":
                where = "lower(coalesce(ts.cccd, '')) = :text";
                break;
            case "SBD":
                where = "lower(coalesce(ts.sobaodanh, '')) = :text";
                break;
            case "DTUT":
                where = "lower(coalesce(dt.maDoituong, '')) = :text";
                break;
            case "KVUT":
                where = "lower(coalesce(kv.maKhuVuc, '')) = :text";
                break;
            case "SDT":
                where = "lower(coalesce(ts.dienThoai, '')) = :text";
                break;
            case "HOTEN":
                where = "lower(concat(concat(coalesce(ts.ho, ''), ' '), coalesce(ts.ten, ''))) like :kw";
                like = true;
                break;
            case "EMAIL":
                where = "lower(coalesce(ts.email, '')) like :kw";
                like = true;
                break;
            default:
                where = "lower(coalesce(ts.cccd, '')) = :text " +
                        "or lower(coalesce(ts.sobaodanh, '')) = :text " +
                        "or lower(coalesce(dt.maDoituong, '')) = :text " +
                        "or lower(coalesce(kv.maKhuVuc, '')) = :text " +
                        "or lower(concat(concat(coalesce(ts.ho, ''), ' '), coalesce(ts.ten, ''))) like :kw " +
                        "or lower(coalesce(ts.email, '')) like :kw " +
                        "or lower(coalesce(ts.dienThoai, '')) = :text";
                like = true;
                break;
        }

        jpql += "where (" + where + ") ";
        if (!countOnly) jpql += "order by ts.ten, ts.ho, ts.thisinhId";
        javax.persistence.TypedQuery<R> q;

        if (countOnly) {

            q = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, Long.class);

        } else {

            q = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, ThiSinh.class);

        }
        if (where.contains(":id")) q.setParameter("id", parseIntegerOrNeverMatch(raw));
        if (where.contains(":text")) q.setParameter("text", low);
        if (like || where.contains(":kw")) q.setParameter("kw", "%" + low + "%");
        return q;
    }

    private Integer parseIntegerOrNeverMatch(String value) {
        try { return Integer.parseInt(value.trim()); } catch (Exception e) { return -1; }
    }

    public List<ThiSinh> findByNguoiDungId(Integer nguoidungId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<ThiSinh> cq = cb.createQuery(ThiSinh.class);
        Root<ThiSinh> root = cq.from(ThiSinh.class);
        Join<ThiSinh, ?> nguoiDung = root.join("nguoiDung");
        cq.select(root).where(cb.equal(nguoiDung.get("nguoidungId"), nguoidungId));
        return em().createQuery(cq).getResultList();
    }

    public String generateSoBaoDanh() {
        try (Session session = HibernateUtil.getSession()) {
            Object result = session.createNativeQuery(
                    "SELECT MAX(CAST(SUBSTRING(sobaodanh, 3) AS UNSIGNED)) " +
                            "FROM xt_thisinh " +
                            "WHERE sobaodanh LIKE 'TS%'"
            ).uniqueResult();

            int nextNumber = 1;
            if (result != null) {
                nextNumber = ((Number) result).intValue() + 1;
            }

            return String.format("TS%05d", nextNumber);
        }
    }
}
