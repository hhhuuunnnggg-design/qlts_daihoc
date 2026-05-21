package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.IDiemCongDao;
import com.tuyensinh.entity.DiemCong;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

public class DiemCongDao extends BaseDao<DiemCong> implements IDiemCongDao {

    @Override
    protected Class<DiemCong> getEntityClass() {
        return DiemCong.class;
    }

    @Override
    public List<DiemCong> findByThiSinhId(Integer thisinhId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<DiemCong> cq = cb.createQuery(DiemCong.class);
        Root<DiemCong> root = cq.from(DiemCong.class);
        Join<DiemCong, ?> thiSinhJoin = root.join("thiSinh");
        Join<DiemCong, ?> nganhToHopJoin = root.join("nganhToHop");

        cq.select(root)
                .where(cb.equal(thiSinhJoin.get("thisinhId"), thisinhId))
                .orderBy(cb.asc(nganhToHopJoin.get("nganhTohopId")));

        return em().createQuery(cq).getResultList();
    }

    @Override
    public List<DiemCong> findByPhuongThuc(Short phuongthucId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<DiemCong> cq = cb.createQuery(DiemCong.class);
        Root<DiemCong> root = cq.from(DiemCong.class);
        Join<DiemCong, ?> phuongThucJoin = root.join("phuongThuc");
        Join<DiemCong, ?> thiSinhJoin = root.join("thiSinh");

        cq.select(root)
                .where(cb.equal(phuongThucJoin.get("phuongthucId"), phuongthucId))
                .orderBy(cb.asc(thiSinhJoin.get("ten")), cb.asc(thiSinhJoin.get("ho")));

        return em().createQuery(cq).getResultList();
    }

    @Override
    public List<DiemCong> findByNganhToHopId(Integer nganhToHopId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<DiemCong> cq = cb.createQuery(DiemCong.class);
        Root<DiemCong> root = cq.from(DiemCong.class);
        Join<DiemCong, ?> nganhToHopJoin = root.join("nganhToHop");
        Join<DiemCong, ?> thiSinhJoin = root.join("thiSinh");

        cq.select(root)
                .where(cb.equal(nganhToHopJoin.get("nganhTohopId"), nganhToHopId))
                .orderBy(cb.asc(thiSinhJoin.get("ten")), cb.asc(thiSinhJoin.get("ho")));

        return em().createQuery(cq).getResultList();
    }

    @Override
    public Optional<DiemCong> findByThiSinhNganhToHopPhuongThuc(Integer thisinhId, Integer nganhToHopId, Short phuongthucId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<DiemCong> cq = cb.createQuery(DiemCong.class);
        Root<DiemCong> root = cq.from(DiemCong.class);

        Join<DiemCong, ?> thiSinhJoin = root.join("thiSinh");
        Join<DiemCong, ?> nganhToHopJoin = root.join("nganhToHop");
        Join<DiemCong, ?> phuongThucJoin = root.join("phuongThuc");

        cq.select(root).where(
                cb.and(
                        cb.equal(thiSinhJoin.get("thisinhId"), thisinhId),
                        cb.equal(nganhToHopJoin.get("nganhTohopId"), nganhToHopId),
                        cb.equal(phuongThucJoin.get("phuongthucId"), phuongthucId)
                )
        );

        List<DiemCong> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<DiemCong> findPage(int page, int pageSize) {
        return em().createQuery(
                        "select dc from DiemCong dc " +
                                "left join dc.thiSinh ts " +
                                "left join dc.nganhToHop nt " +
                                "left join nt.nganh n " +
                                "left join nt.toHop th " +
                                "left join dc.phuongThuc pt " +
                                "order by ts.ten, ts.ho, n.maNganh, th.maTohop, pt.phuongthucId, dc.diemcongId",
                        DiemCong.class)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public long countAll() {
        return count();
    }

    public List<DiemCong> searchPage(String keyword, int page, int pageSize) {
        String kw = "%" + normalizeKeyword(keyword) + "%";
        return em().createQuery(
                        "select dc from DiemCong dc " +
                                "left join dc.thiSinh ts " +
                                "left join dc.nganhToHop nt " +
                                "left join nt.nganh n " +
                                "left join nt.toHop th " +
                                "left join dc.phuongThuc pt " +
                                "where lower(coalesce(ts.cccd, '')) like :kw " +
                                "or lower(coalesce(ts.sobaodanh, '')) like :kw " +
                                "or lower(coalesce(ts.ho, '')) like :kw " +
                                "or lower(coalesce(ts.ten, '')) like :kw " +
                                "or lower(coalesce(n.maNganh, '')) like :kw " +
                                "or lower(coalesce(n.tenNganh, '')) like :kw " +
                                "or lower(coalesce(th.maTohop, '')) like :kw " +
                                "or lower(coalesce(pt.maPhuongthuc, '')) like :kw " +
                                "order by ts.ten, ts.ho, n.maNganh, th.maTohop, pt.phuongthucId, dc.diemcongId",
                        DiemCong.class)
                .setParameter("kw", kw)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public long countSearch(String keyword) {
        String kw = "%" + normalizeKeyword(keyword) + "%";
        return em().createQuery(
                        "select count(dc) from DiemCong dc " +
                                "left join dc.thiSinh ts " +
                                "left join dc.nganhToHop nt " +
                                "left join nt.nganh n " +
                                "left join nt.toHop th " +
                                "left join dc.phuongThuc pt " +
                                "where lower(coalesce(ts.cccd, '')) like :kw " +
                                "or lower(coalesce(ts.sobaodanh, '')) like :kw " +
                                "or lower(coalesce(ts.ho, '')) like :kw " +
                                "or lower(coalesce(ts.ten, '')) like :kw " +
                                "or lower(coalesce(n.maNganh, '')) like :kw " +
                                "or lower(coalesce(n.tenNganh, '')) like :kw " +
                                "or lower(coalesce(th.maTohop, '')) like :kw " +
                                "or lower(coalesce(pt.maPhuongthuc, '')) like :kw",
                        Long.class)
                .setParameter("kw", kw)
                .getSingleResult();
    }



    public List<DiemCong> searchPageByField(String field, String keyword, int page, int pageSize) {
        javax.persistence.TypedQuery<DiemCong> query = buildSearchByFieldQuery(field, keyword, false);
        query.setFirstResult((Math.max(1, page) - 1) * pageSize);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }

    public long countSearchByField(String field, String keyword) {
        javax.persistence.TypedQuery<Long> query = buildSearchByFieldQuery(field, keyword, true);
        return query.getSingleResult();
    }

    @SuppressWarnings("unchecked")
    private <R> javax.persistence.TypedQuery<R> buildSearchByFieldQuery(String field, String keyword, boolean countOnly) {
        String f = field == null ? "ALL" : field.trim().toUpperCase();
        String raw = keyword == null ? "" : keyword.trim();
        String low = raw.toLowerCase();

        String select = countOnly ? "select count(dc) " : "select dc ";
        String jpql = select +
                "from DiemCong dc " +
                "left join dc.thiSinh ts " +
                "left join dc.nganhToHop nt " +
                "left join nt.nganh n " +
                "left join nt.toHop th " +
                "left join dc.phuongThuc pt ";
        String where;
        boolean like = false;
        switch (f) {
            case "ID":
                where = "dc.diemcongId = :id";
                break;
            case "CCCD":
                where = "lower(coalesce(ts.cccd, '')) = :text";
                break;
            case "SBD":
                where = "lower(coalesce(ts.sobaodanh, '')) = :text";
                break;
            case "NV":
                where = "exists (select nv.nguyenvongId from NguyenVong nv where nv.thiSinh = ts and nv.nganhToHop = nt and nv.thuTu = :thuTu)";
                break;
            case "MANGANH":
                where = "lower(coalesce(n.maNganh, '')) = :text";
                break;
            case "MATOHOP":
                where = "lower(coalesce(th.maTohop, '')) = :text";
                break;
            case "MAPT":
                where = "lower(coalesce(pt.maPhuongthuc, '')) = :text";
                break;
            case "HOTEN":
                where = "lower(concat(concat(coalesce(ts.ho, ''), ' '), coalesce(ts.ten, ''))) like :kw";
                like = true;
                break;
            default:
                where = "lower(coalesce(ts.cccd, '')) = :text " +
                        "or lower(coalesce(ts.sobaodanh, '')) = :text " +
                        "or lower(coalesce(n.maNganh, '')) = :text " +
                        "or lower(coalesce(th.maTohop, '')) = :text " +
                        "or lower(coalesce(pt.maPhuongthuc, '')) = :text " +
                        "or lower(concat(concat(coalesce(ts.ho, ''), ' '), coalesce(ts.ten, ''))) like :kw " +
                        "or lower(coalesce(n.tenNganh, '')) like :kw";
                like = true;
                break;
        }
        jpql += "where (" + where + ") ";
        if (!countOnly) {
            jpql += "order by ts.ten, ts.ho, n.maNganh, th.maTohop, pt.phuongthucId, dc.diemcongId";
        }
        javax.persistence.TypedQuery<R> q;

        if (countOnly) {

            q = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, Long.class);

        } else {

            q = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, DiemCong.class);

        }
        if (where.contains(":id")) q.setParameter("id", parseIntegerOrNeverMatch(raw));
        if (where.contains(":thuTu")) q.setParameter("thuTu", parseShortOrNeverMatch(raw));
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

    /**
     * Xoa nhanh toan bo diem cong va chi tiet diem cong trong DB.
     * Dung DELETE bulk thay vi duyet tung entity de tranh mat hang gio khi co hang tram nghin dong.
     * Khong dung TRUNCATE vi MySQL co the chan bang cha xt_diemcong do FK tu xt_diemcong_chitiet.
     */
    public void deleteAllFast() {
        EntityManager em = em();
        em.getTransaction().begin();
        try {
            em.createNativeQuery("DELETE FROM xt_diemcong_chitiet").executeUpdate();
            em.createNativeQuery("DELETE FROM xt_diemcong").executeUpdate();
            em.createNativeQuery("ALTER TABLE xt_diemcong_chitiet AUTO_INCREMENT = 1").executeUpdate();
            em.createNativeQuery("ALTER TABLE xt_diemcong AUTO_INCREMENT = 1").executeUpdate();
            em.flush();
            em.clear();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Luu DiemCong theo lo trong mot transaction.
     * DiemCong.chiTietList duoc persist kem theo nho CascadeType.ALL tren entity DiemCong.
     */
    public void saveBatch(List<DiemCong> list, int batchSize) {
        if (list == null || list.isEmpty()) return;

        int safeBatchSize = batchSize > 0 ? batchSize : 500;
        EntityManager em = em();
        em.getTransaction().begin();
        try {
            int count = 0;
            for (DiemCong dc : list) {
                if (dc == null) continue;
                em.persist(dc);
                count++;

                if (count % safeBatchSize == 0) {
                    em.flush();
                    em.clear();
                }
            }
            em.flush();
            em.clear();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }


}