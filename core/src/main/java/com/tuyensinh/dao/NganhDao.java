package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.INganhDao;
import com.tuyensinh.entity.Nganh;
import com.tuyensinh.util.HibernateUtil;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NganhDao extends BaseDao<Nganh> implements INganhDao {

    @Override
    protected Class<Nganh> getEntityClass() {
        return Nganh.class;
    }

    public Optional<Nganh> findByMa(String maNganh) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<Nganh> cq = cb.createQuery(Nganh.class);
        Root<Nganh> root = cq.from(Nganh.class);
        cq.select(root).where(cb.equal(root.get("maNganh"), maNganh));
        List<Nganh> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Nganh> findActive() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<Nganh> cq = cb.createQuery(Nganh.class);
        Root<Nganh> root = cq.from(Nganh.class);
        cq.select(root).where(cb.equal(root.get("isActive"), true));
        cq.orderBy(cb.asc(root.get("maNganh")));
        return em().createQuery(cq).getResultList();
    }

    public List<Nganh> searchByMaOrTen(String keyword) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<Nganh> cq = cb.createQuery(Nganh.class);
        Root<Nganh> root = cq.from(Nganh.class);
        String kw = "%" + keyword + "%";
        Predicate p1 = cb.like(root.get("maNganh"), kw);
        Predicate p2 = cb.like(root.get("tenNganh"), kw);
        cq.select(root).where(cb.or(p1, p2));
        cq.orderBy(cb.asc(root.get("maNganh")));
        return em().createQuery(cq).getResultList();
    }

    public List<Nganh> findByPage(int page, int pageSize) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<Nganh> cq = cb.createQuery(Nganh.class);
        Root<Nganh> root = cq.from(Nganh.class);
        cq.select(root).orderBy(cb.asc(root.get("maNganh")));
        TypedQuery<Nganh> q = em().createQuery(cq);
        q.setFirstResult((page - 1) * pageSize);
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    public long countAll() {
        return count();
    }

    public List<Object[]> thongKeNganh() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<Nganh> root = cq.from(Nganh.class);
        Join<Nganh, ?> npt = root.join("danhSachNganhPhuongThuc", JoinType.LEFT);
        cq.multiselect(
                root.get("maNganh"),
                root.get("tenNganh"),
                root.get("chiTieu"),
                root.get("diemSan"),
                cb.count(npt.get("nganhPhuongthucId"))
        );
        cq.groupBy(root.get("nganhId")).orderBy(cb.asc(root.get("maNganh")));
        return em().createQuery(cq).getResultList();
    }

    /**
     * Cap nhat diem_trung_tuyen cua xt_nganh theo lo.
     * Key = nganh_id, value = diem_trung_tuyen. Value null se xoa diem trung tuyen cu.
     */
    public void updateDiemTrungTuyenBatch(Map<Integer, BigDecimal> diemTheoNganh) {
        if (diemTheoNganh == null || diemTheoNganh.isEmpty()) return;

        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            int count = 0;
            for (Map.Entry<Integer, BigDecimal> entry : diemTheoNganh.entrySet()) {
                Integer nganhId = entry.getKey();
                if (nganhId == null) continue;

                em.createQuery(
                                "UPDATE Nganh n " +
                                        "SET n.diemTrungTuyen = :diemTrungTuyen " +
                                        "WHERE n.nganhId = :nganhId")
                        .setParameter("diemTrungTuyen", entry.getValue())
                        .setParameter("nganhId", nganhId)
                        .executeUpdate();

                count++;
                if (count % 200 == 0) {
                    em.flush();
                    em.clear();
                }
            }

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }


    public List<Nganh> searchByMaOrTenPage(String keyword, int page, int pageSize) {
        String kw = "%" + normalizeKeyword(keyword) + "%";
        return em().createQuery(
                        "select n from Nganh n " +
                                "where lower(coalesce(n.maNganh, '')) like :kw " +
                                "or lower(coalesce(n.tenNganh, '')) like :kw " +
                                "order by n.maNganh, n.nganhId",
                        Nganh.class)
                .setParameter("kw", kw)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public long countSearchByMaOrTen(String keyword) {
        String kw = "%" + normalizeKeyword(keyword) + "%";
        return em().createQuery(
                        "select count(n) from Nganh n " +
                                "where lower(coalesce(n.maNganh, '')) like :kw " +
                                "or lower(coalesce(n.tenNganh, '')) like :kw",
                        Long.class)
                .setParameter("kw", kw)
                .getSingleResult();
    }



    public List<Nganh> searchPageByField(String field, String keyword, int page, int pageSize) {
        javax.persistence.TypedQuery<Nganh> q = buildSearchByFieldQuery(field, keyword, false);
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
        String select = countOnly ? "select count(distinct n) " : "select distinct n ";
        String jpql = select + "from Nganh n " +
                "left join n.toHopGoc th " +
                "left join n.danhSachNganhPhuongThuc npt " +
                "left join npt.phuongThuc pt ";
        String where;
        boolean like = false;
        switch (f) {
            case "ID":
                where = "n.nganhId = :id";
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
            case "TENNGANH":
                where = "lower(coalesce(n.tenNganh, '')) like :kw";
                like = true;
                break;
            default:
                where = "lower(coalesce(n.maNganh, '')) = :text " +
                        "or lower(coalesce(th.maTohop, '')) = :text " +
                        "or lower(coalesce(pt.maPhuongthuc, '')) = :text " +
                        "or lower(coalesce(n.tenNganh, '')) like :kw";
                like = true;
                break;
        }
        jpql += "where (" + where + ") ";
        if (!countOnly) jpql += "order by n.maNganh, n.nganhId";
        javax.persistence.TypedQuery<R> q;

        if (countOnly) {

            q = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, Long.class);

        } else {

            q = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, Nganh.class);

        }
        if (where.contains(":id")) q.setParameter("id", parseIntegerOrNeverMatch(raw));
        if (where.contains(":text")) q.setParameter("text", low);
        if (like || where.contains(":kw")) q.setParameter("kw", "%" + low + "%");
        return q;
    }

    private Integer parseIntegerOrNeverMatch(String value) {
        try { return Integer.parseInt(value.trim()); } catch (Exception e) { return -1; }
    }

    /**
     * Lay danh sach ma phuong thuc dang gan cho nganh de hien thi tren panel Nganh.
     * Dung native SQL vi MySQL co GROUP_CONCAT, giup gom THPT/VSAT/DGNL tren 1 dong.
     */
    public String findPhuongThucTextByNganhId(Integer nganhId) {
        if (nganhId == null) return "";

        Object result = em().createNativeQuery(
                        "SELECT GROUP_CONCAT(DISTINCT pt.ma_phuongthuc ORDER BY pt.ma_phuongthuc SEPARATOR ', ') " +
                                "FROM xt_nganh_phuongthuc npt " +
                                "JOIN xt_phuongthuc pt ON pt.phuongthuc_id = npt.phuongthuc_id " +
                                "WHERE npt.nganh_id = :nganhId " +
                                "AND COALESCE(npt.is_enabled, 1) = 1")
                .setParameter("nganhId", nganhId)
                .getSingleResult();

        return result != null ? String.valueOf(result) : "";
    }

    /** Dem so nguyen vong dang ky vao nganh. */
    public long countNguyenVongByNganhId(Integer nganhId) {
        if (nganhId == null) return 0L;

        return em().createQuery(
                        "select count(nv) from NguyenVong nv " +
                                "where nv.nganh.nganhId = :nganhId",
                        Long.class)
                .setParameter("nganhId", nganhId)
                .getSingleResult();
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase();
    }

}