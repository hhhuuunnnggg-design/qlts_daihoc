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



    public List<ThiSinhThanhTich> searchPageByField(String field, String keyword, int page, int pageSize) {
        javax.persistence.TypedQuery<ThiSinhThanhTich> q = buildSearchByFieldQuery(field, keyword, false);
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
        String select = countOnly ? "select count(tt) " : "select tt ";
        String jpql = select + "from ThiSinhThanhTich tt left join tt.thiSinh ts ";
        String where;
        boolean like = false;
        switch (f) {
            case "ID": where = "tt.thanhtichId = :id"; break;
            case "CCCD": where = "lower(coalesce(ts.cccd, '')) = :text"; break;
            case "SBD": where = "lower(coalesce(ts.sobaodanh, '')) = :text"; break;
            case "NHOM": where = "lower(coalesce(tt.nhomThanhTich, '')) = :text"; break;
            case "CAP": where = "lower(coalesce(tt.capThanhTich, '')) = :text"; break;
            case "GIAI": where = "lower(coalesce(tt.loaiGiai, '')) = :text"; break;
            case "MON": where = "lower(coalesce(tt.monDatGiai, '')) = :text"; break;
            case "NAM": where = "tt.namDatGiai = :nam"; break;
            case "XACMINH": where = "lower(coalesce(tt.trangThaiXacMinh, '')) = :text"; break;
            case "HOTEN": where = "lower(concat(concat(coalesce(ts.ho, ''), ' '), coalesce(ts.ten, ''))) like :kw"; like = true; break;
            default:
                where = "lower(coalesce(ts.cccd, '')) = :text or lower(coalesce(ts.sobaodanh, '')) = :text " +
                        "or lower(coalesce(tt.nhomThanhTich, '')) = :text or lower(coalesce(tt.capThanhTich, '')) = :text " +
                        "or lower(coalesce(tt.loaiGiai, '')) = :text or lower(coalesce(tt.monDatGiai, '')) = :text " +
                        "or lower(coalesce(tt.trangThaiXacMinh, '')) = :text " +
                        "or lower(concat(concat(coalesce(ts.ho, ''), ' '), coalesce(ts.ten, ''))) like :kw " +
                        "or lower(coalesce(tt.tenThanhTich, '')) like :kw or lower(coalesce(tt.ghiChu, '')) like :kw";
                like = true; break;
        }
        jpql += "where (" + where + ") ";
        if (!countOnly) jpql += "order by tt.thanhtichId";
        javax.persistence.TypedQuery<R> q;

        if (countOnly) {

            q = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, Long.class);

        } else {

            q = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, ThiSinhThanhTich.class);

        }
        if (where.contains(":id")) q.setParameter("id", parseIntegerOrNeverMatch(raw));
        if (where.contains(":nam")) q.setParameter("nam", parseShortOrNeverMatch(raw));
        if (where.contains(":text")) q.setParameter("text", low);
        if (like || where.contains(":kw")) q.setParameter("kw", "%" + low + "%");
        return q;
    }

    private Integer parseIntegerOrNeverMatch(String value) {
        try { return Integer.parseInt(value.trim()); } catch (Exception e) { return -1; }
    }

    private Short parseShortOrNeverMatch(String value) {
        try { return Short.parseShort(value.trim()); } catch (Exception e) { return Short.MIN_VALUE; }
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase();
    }

}