package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.INguyenVongDao;
import com.tuyensinh.entity.NguyenVong;
import com.tuyensinh.util.HibernateUtil;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NguyenVongDao extends BaseDao<NguyenVong> implements INguyenVongDao {

    @Override
    protected Class<NguyenVong> getEntityClass() {
        return NguyenVong.class;
    }

    public List<NguyenVong> findByThiSinhId(Integer thisinhId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NguyenVong> cq = cb.createQuery(NguyenVong.class);
        Root<NguyenVong> root = cq.from(NguyenVong.class);
        Join<NguyenVong, ?> thiSinh = root.join("thiSinh");
        cq.select(root).where(cb.equal(thiSinh.get("thisinhId"), thisinhId));
        cq.orderBy(cb.asc(root.get("thuTu")));
        return em().createQuery(cq).getResultList();
    }

    public List<NguyenVong> findByNganhId(Integer nganhId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NguyenVong> cq = cb.createQuery(NguyenVong.class);
        Root<NguyenVong> root = cq.from(NguyenVong.class);
        Join<NguyenVong, ?> nganh = root.join("nganh");
        cq.select(root).where(cb.equal(nganh.get("nganhId"), nganhId));
        cq.orderBy(cb.desc(root.get("diemXettuyen")));
        return em().createQuery(cq).getResultList();
    }

    public List<NguyenVong> findByNganhIdAndPhuongThuc(Integer nganhId, Short phuongthucId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NguyenVong> cq = cb.createQuery(NguyenVong.class);
        Root<NguyenVong> root = cq.from(NguyenVong.class);
        Join<NguyenVong, ?> nganh = root.join("nganh");
        Join<NguyenVong, ?> phuongThuc = root.join("phuongThuc");
        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(nganh.get("nganhId"), nganhId));
        preds.add(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));
        cq.select(root).where(preds.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("diemXettuyen")));
        return em().createQuery(cq).getResultList();
    }

    public Optional<NguyenVong> findByThiSinhNganhToHopPhuongThuc(
            Integer thisinhId, Integer nganhId, Integer nganhToHopId, Short phuongthucId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NguyenVong> cq = cb.createQuery(NguyenVong.class);
        Root<NguyenVong> root = cq.from(NguyenVong.class);
        Join<NguyenVong, ?> thiSinh = root.join("thiSinh");
        Join<NguyenVong, ?> nganh = root.join("nganh");
        Join<NguyenVong, ?> nganhToHop = root.join("nganhToHop");
        Join<NguyenVong, ?> phuongThuc = root.join("phuongThuc");
        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(thiSinh.get("thisinhId"), thisinhId));
        preds.add(cb.equal(nganh.get("nganhId"), nganhId));
        preds.add(cb.equal(nganhToHop.get("nganhTohopId"), nganhToHopId));
        preds.add(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));
        cq.select(root).where(preds.toArray(new Predicate[0]));
        List<NguyenVong> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<NguyenVong> findByKetQua(String ketQua) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NguyenVong> cq = cb.createQuery(NguyenVong.class);
        Root<NguyenVong> root = cq.from(NguyenVong.class);
        cq.select(root).where(cb.equal(root.get("ketQua"), ketQua));
        cq.orderBy(cb.desc(root.get("diemXettuyen")));
        return em().createQuery(cq).getResultList();
    }

    public List<NguyenVong> findAll() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NguyenVong> cq = cb.createQuery(NguyenVong.class);
        Root<NguyenVong> root = cq.from(NguyenVong.class);
        Join<NguyenVong, ?> thiSinh = root.join("thiSinh");
        cq.select(root).orderBy(cb.asc(thiSinh.get("ten")), cb.asc(thiSinh.get("ho")), cb.asc(root.get("thuTu")));
        return em().createQuery(cq).getResultList();
    }

    /**
     * Load day du cac quan he can dung khi tinh diem/xet tuyen.
     * Neu dung findAll() cu, cac quan he LAZY nhu maXetTuyenMap/nganhToHop/phuongThuc
     * co the bi detached va gay loi: could not initialize proxy ... no Session.
     */
    public List<NguyenVong> findAllForXetTuyen() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NguyenVong> cq = cb.createQuery(NguyenVong.class);
        Root<NguyenVong> root = cq.from(NguyenVong.class);

        Fetch<?, ?> thiSinhFetch = root.fetch("thiSinh", JoinType.LEFT);
        thiSinhFetch.fetch("khuVucUutien", JoinType.LEFT);
        thiSinhFetch.fetch("doiTuongUutien", JoinType.LEFT);

        Fetch<?, ?> maXtFetch = root.fetch("maXetTuyenMap", JoinType.LEFT);
        maXtFetch.fetch("nganh", JoinType.LEFT);
        maXtFetch.fetch("phuongThuc", JoinType.LEFT);
        Fetch<?, ?> maXtNthFetch = maXtFetch.fetch("nganhToHop", JoinType.LEFT);
        maXtNthFetch.fetch("toHop", JoinType.LEFT);

        root.fetch("nganh", JoinType.LEFT);
        Fetch<?, ?> nthFetch = root.fetch("nganhToHop", JoinType.LEFT);
        nthFetch.fetch("toHop", JoinType.LEFT);
        Fetch<?, ?> nthmFetch = nthFetch.fetch("danhSachNganhToHopMon", JoinType.LEFT);
        nthmFetch.fetch("mon", JoinType.LEFT);
        root.fetch("phuongThuc", JoinType.LEFT);

        Join<NguyenVong, ?> thiSinh = root.join("thiSinh", JoinType.LEFT);
        cq.select(root).distinct(true);
        cq.orderBy(cb.asc(thiSinh.get("ten")), cb.asc(thiSinh.get("ho")), cb.asc(root.get("thuTu")));

        return em().createQuery(cq).getResultList();
    }

    public int countByNganhAndPhuongThuc(Integer nganhId, Short phuongthucId, String ketQua) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<NguyenVong> root = cq.from(NguyenVong.class);
        Join<NguyenVong, ?> nganh = root.join("nganh");
        Join<NguyenVong, ?> phuongThuc = root.join("phuongThuc");
        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(nganh.get("nganhId"), nganhId));
        preds.add(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));
        preds.add(cb.equal(root.get("ketQua"), ketQua));
        cq.select(cb.count(root)).where(preds.toArray(new Predicate[0]));
        Long result = em().createQuery(cq).getSingleResult();
        return result.intValue();
    }

    /** Phan trang that o DB cho cac panel hien thi danh sach nguyen vong/xet tuyen. */
    public List<NguyenVong> findPage(int page, int pageSize) {
        return em().createQuery(
                        "select nv from NguyenVong nv " +
                                "left join fetch nv.thiSinh ts " +
                                "left join fetch nv.nganh n " +
                                "left join fetch nv.nganhToHop nt " +
                                "left join fetch nt.toHop th " +
                                "left join fetch nv.phuongThuc pt " +
                                "order by ts.ten, ts.ho, nv.thuTu, nv.nguyenvongId",
                        NguyenVong.class)
                .setFirstResult((Math.max(1, page) - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public long countAll() {
        return em().createQuery("select count(nv) from NguyenVong nv", Long.class)
                .getSingleResult();
    }

    public List<NguyenVong> searchPage(String keyword, int page, int pageSize) {
        String kw = "%" + normalizeKeyword(keyword) + "%";
        return em().createQuery(
                        "select nv from NguyenVong nv " +
                                "left join fetch nv.thiSinh ts " +
                                "left join fetch nv.nganh n " +
                                "left join fetch nv.nganhToHop nt " +
                                "left join fetch nt.toHop th " +
                                "left join fetch nv.phuongThuc pt " +
                                "where lower(coalesce(ts.cccd, '')) like :kw " +
                                "or lower(coalesce(ts.sobaodanh, '')) like :kw " +
                                "or lower(coalesce(ts.ho, '')) like :kw " +
                                "or lower(coalesce(ts.ten, '')) like :kw " +
                                "or lower(coalesce(n.maNganh, '')) like :kw " +
                                "or lower(coalesce(n.tenNganh, '')) like :kw " +
                                "or lower(coalesce(th.maTohop, '')) like :kw " +
                                "or lower(coalesce(th.tenTohop, '')) like :kw " +
                                "or lower(coalesce(nv.toHopDiemTotNhat, '')) like :kw " +
                                "or lower(coalesce(pt.maPhuongthuc, '')) like :kw " +
                                "or lower(coalesce(pt.tenPhuongthuc, '')) like :kw " +
                                "or lower(coalesce(nv.ketQua, '')) like :kw " +
                                "or lower(coalesce(nv.phuongThucDiemTotNhat, '')) like :kw " +
                                "order by ts.ten, ts.ho, nv.thuTu, nv.nguyenvongId",
                        NguyenVong.class)
                .setParameter("kw", kw)
                .setFirstResult((Math.max(1, page) - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public long countSearch(String keyword) {
        String kw = "%" + normalizeKeyword(keyword) + "%";
        return em().createQuery(
                        "select count(nv) from NguyenVong nv " +
                                "left join nv.thiSinh ts " +
                                "left join nv.nganh n " +
                                "left join nv.nganhToHop nt " +
                                "left join nt.toHop th " +
                                "left join nv.phuongThuc pt " +
                                "where lower(coalesce(ts.cccd, '')) like :kw " +
                                "or lower(coalesce(ts.sobaodanh, '')) like :kw " +
                                "or lower(coalesce(ts.ho, '')) like :kw " +
                                "or lower(coalesce(ts.ten, '')) like :kw " +
                                "or lower(coalesce(n.maNganh, '')) like :kw " +
                                "or lower(coalesce(n.tenNganh, '')) like :kw " +
                                "or lower(coalesce(th.maTohop, '')) like :kw " +
                                "or lower(coalesce(th.tenTohop, '')) like :kw " +
                                "or lower(coalesce(nv.toHopDiemTotNhat, '')) like :kw " +
                                "or lower(coalesce(pt.maPhuongthuc, '')) like :kw " +
                                "or lower(coalesce(pt.tenPhuongthuc, '')) like :kw " +
                                "or lower(coalesce(nv.ketQua, '')) like :kw " +
                                "or lower(coalesce(nv.toHopDiemTotNhat, '')) like :kw " +
                                "or lower(coalesce(nv.phuongThucDiemTotNhat, '')) like :kw",
                        Long.class)
                .setParameter("kw", kw)
                .getSingleResult();
    }



    public List<NguyenVong> searchPageByField(String field, String keyword, int page, int pageSize) {
        javax.persistence.TypedQuery<NguyenVong> query = buildSearchByFieldQuery(field, keyword, false);
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

        String select = countOnly ? "select count(nv) " : "select nv ";
        String fetch = countOnly ? "" : "fetch ";
        String jpql = select +
                "from NguyenVong nv " +
                "left join " + fetch + "nv.thiSinh ts " +
                "left join " + fetch + "nv.nganh n " +
                "left join " + fetch + "nv.nganhToHop nt " +
                "left join " + fetch + "nt.toHop th " +
                "left join " + fetch + "nv.phuongThuc pt " +
                "left join nv.maXetTuyenMap mx ";
        String where;
        boolean like = false;
        switch (f) {
            case "ID":
                where = "nv.nguyenvongId = :id";
                break;
            case "CCCD":
                where = "lower(coalesce(ts.cccd, '')) = :text";
                break;
            case "SBD":
                where = "lower(coalesce(ts.sobaodanh, '')) = :text";
                break;
            case "NV":
                where = "nv.thuTu = :thuTu";
                break;
            case "MAXT":
                where = "lower(coalesce(mx.maXetTuyen, '')) = :text";
                break;
            case "MANGANH":
                where = "lower(coalesce(n.maNganh, '')) = :text";
                break;
            case "MATOHOP":
                where = "lower(coalesce(th.maTohop, '')) = :text";
                break;
            case "THMTOTNHAT":
                where = "lower(coalesce(nv.toHopDiemTotNhat, '')) = :text";
                break;
            case "MAPT":
                where = "lower(coalesce(pt.maPhuongthuc, '')) = :text";
                break;
            case "KETQUA":
                where = "lower(coalesce(nv.ketQua, '')) = :text";
                break;
            case "NGUONDIEM":
                where = "lower(coalesce(nv.phuongThucDiemTotNhat, '')) = :text";
                break;
            case "HOTEN":
                where = "lower(concat(concat(coalesce(ts.ho, ''), ' '), coalesce(ts.ten, ''))) like :kw";
                like = true;
                break;
            default:
                where = "lower(coalesce(ts.cccd, '')) = :text " +
                        "or lower(coalesce(ts.sobaodanh, '')) = :text " +
                        "or lower(coalesce(mx.maXetTuyen, '')) = :text " +
                        "or lower(coalesce(n.maNganh, '')) = :text " +
                        "or lower(coalesce(th.maTohop, '')) = :text " +
                        "or lower(coalesce(nv.toHopDiemTotNhat, '')) = :text " +
                        "or lower(coalesce(pt.maPhuongthuc, '')) = :text " +
                        "or lower(coalesce(nv.ketQua, '')) = :text " +
                        "or lower(coalesce(nv.phuongThucDiemTotNhat, '')) = :text " +
                        "or lower(concat(concat(coalesce(ts.ho, ''), ' '), coalesce(ts.ten, ''))) like :kw " +
                        "or lower(coalesce(n.tenNganh, '')) like :kw";
                like = true;
                break;
        }
        jpql += "where (" + where + ") ";
        if (!countOnly) jpql += "order by ts.ten, ts.ho, nv.thuTu, nv.nguyenvongId";
        javax.persistence.TypedQuery<R> q;

        if (countOnly) {

            q = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, Long.class);

        } else {

            q = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, NguyenVong.class);

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

    /**
     * Danh sach trung tuyen chi tiet, sap xep theo nganh de hien thi bang phu trong panel Xet tuyen.
     */
    public List<NguyenVong> findTrungTuyenChiTietTheoNganh(int maxRows) {
        int limit = maxRows <= 0 ? 10000 : maxRows;
        return em().createQuery(
                        "select nv from NguyenVong nv " +
                                "left join fetch nv.thiSinh ts " +
                                "left join fetch nv.nganh n " +
                                "left join fetch nv.nganhToHop nt " +
                                "left join fetch nt.toHop th " +
                                "left join fetch nv.phuongThuc pt " +
                                "where nv.ketQua = :ketQua " +
                                "order by n.maNganh, nv.phuongThucDiemTotNhat, nv.diemXettuyen desc, ts.ten, ts.ho, nv.thuTu",
                        NguyenVong.class)
                .setParameter("ketQua", NguyenVong.KetQua.TRUNG_TUYEN)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * Thong ke so luong trung tuyen theo nganh va theo nguon diem tot nhat
     * (THPT/VSAT/DGNL). Day la cot co y nghia nhat sau khi he thong so sanh diem tot nhat.
     */
    public List<Object[]> thongKeTrungTuyenTheoNganhPhuongThuc() {
        return em().createQuery(
                        "select n.maNganh, n.tenNganh, coalesce(nv.phuongThucDiemTotNhat, 'CHUA_RO'), count(nv) " +
                                "from NguyenVong nv " +
                                "join nv.nganh n " +
                                "where nv.ketQua = :ketQua " +
                                "group by n.maNganh, n.tenNganh, nv.phuongThucDiemTotNhat " +
                                "order by n.maNganh, nv.phuongThucDiemTotNhat",
                        Object[].class)
                .setParameter("ketQua", NguyenVong.KetQua.TRUNG_TUYEN)
                .getResultList();
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase();
    }

    /**
     * Cap nhat ket qua xet tuyen theo lo lon trong 1 transaction.
     * Tranh viec chay hang chuc nghin UPDATE moi dong 1 transaction lam app treo rat lau.
     */
    public void updateXetTuyenBatch(List<NguyenVong> list) {
        if (list == null || list.isEmpty()) return;

        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            int count = 0;
            for (NguyenVong nv : list) {
                if (nv == null || nv.getNguyenvongId() == null) continue;

                em.createQuery(
                                "UPDATE NguyenVong nv " +
                                        "SET nv.diemThxt = :diemThxt, " +
                                        "    nv.diemCong = :diemCong, " +
                                        "    nv.diemUutien = :diemUutien, " +
                                        "    nv.diemXettuyen = :diemXettuyen, " +
                                        "    nv.phuongThucDiemTotNhat = :phuongThucDiemTotNhat, " +
                                        "    nv.toHopDiemTotNhat = :toHopDiemTotNhat, " +
                                        "    nv.ketQua = :ketQua, " +
                                        "    nv.ghiChu = :ghiChu " +
                                        "WHERE nv.nguyenvongId = :id")
                        .setParameter("diemThxt", nv.getDiemThxt())
                        .setParameter("diemCong", nv.getDiemCong())
                        .setParameter("diemUutien", nv.getDiemUutien())
                        .setParameter("diemXettuyen", nv.getDiemXettuyen())
                        .setParameter("phuongThucDiemTotNhat", nv.getPhuongThucDiemTotNhat())
                        .setParameter("toHopDiemTotNhat", nv.getToHopDiemTotNhat())
                        .setParameter("ketQua", nv.getKetQua())
                        .setParameter("ghiChu", nv.getGhiChu())
                        .setParameter("id", nv.getNguyenvongId())
                        .executeUpdate();

                count++;
                if (count % 500 == 0) {
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
}
