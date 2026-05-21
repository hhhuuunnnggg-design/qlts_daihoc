package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.IDiemThiDao;
import com.tuyensinh.entity.DiemThi;
import com.tuyensinh.entity.DiemThiChiTiet;
import com.tuyensinh.entity.Mon;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DiemThiDao extends BaseDao<DiemThi> implements IDiemThiDao {

    @Override
    protected Class<DiemThi> getEntityClass() {
        return DiemThi.class;
    }

    public Optional<DiemThi> findByThiSinhAndPhuongThuc(Integer thisinhId, Short phuongthucId, Short namTuyensinh) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<DiemThi> cq = cb.createQuery(DiemThi.class);
        Root<DiemThi> root = cq.from(DiemThi.class);
        Join<DiemThi, ?> thiSinh = root.join("thiSinh");
        Join<DiemThi, ?> phuongThuc = root.join("phuongThuc");
        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(thiSinh.get("thisinhId"), thisinhId));
        preds.add(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));
        preds.add(cb.equal(root.get("namTuyensinh"), namTuyensinh));
        cq.select(root).where(preds.toArray(new Predicate[0]));
        List<DiemThi> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * Load mot phieu diem kem day du diem chi tiet + mon.
     * Dung cho TinhDiemService de tranh LazyInitializationException khi lay danhSachDiemChiTiet.
     */
    public Optional<DiemThi> findByThiSinhAndPhuongThucWithDetails(Integer thisinhId, Short phuongthucId, Short namTuyensinh) {
        List<DiemThi> list = em().createQuery(
                        "select distinct d " +
                                "from DiemThi d " +
                                "join fetch d.thiSinh ts " +
                                "join fetch d.phuongThuc pt " +
                                "left join fetch d.danhSachDiemChiTiet ct " +
                                "left join fetch ct.mon m " +
                                "where ts.thisinhId = :thisinhId " +
                                "and pt.phuongthucId = :phuongthucId " +
                                "and d.namTuyensinh = :nam", DiemThi.class)
                .setParameter("thisinhId", thisinhId)
                .setParameter("phuongthucId", phuongthucId)
                .setParameter("nam", namTuyensinh)
                .setMaxResults(1)
                .getResultList();

        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<DiemThi> findByThiSinhId(Integer thisinhId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<DiemThi> cq = cb.createQuery(DiemThi.class);
        Root<DiemThi> root = cq.from(DiemThi.class);
        Join<DiemThi, ?> thiSinh = root.join("thiSinh");
        Join<DiemThi, ?> phuongThuc = root.join("phuongThuc");
        cq.select(root).where(cb.equal(thiSinh.get("thisinhId"), thisinhId));
        cq.orderBy(cb.asc(phuongThuc.get("phuongthucId")));
        return em().createQuery(cq).getResultList();
    }

    public List<DiemThi> findByPhuongThuc(Short phuongthucId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<DiemThi> cq = cb.createQuery(DiemThi.class);
        Root<DiemThi> root = cq.from(DiemThi.class);
        Join<DiemThi, ?> thiSinh = root.join("thiSinh");
        Join<DiemThi, ?> phuongThuc = root.join("phuongThuc");
        cq.select(root).where(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));
        cq.orderBy(cb.asc(thiSinh.get("ten")), cb.asc(thiSinh.get("ho")));
        return em().createQuery(cq).getResultList();
    }

    public List<DiemThi> findByPhuongThucAndPage(Short phuongthucId, int page, int pageSize) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<DiemThi> cq = cb.createQuery(DiemThi.class);
        Root<DiemThi> root = cq.from(DiemThi.class);
        Join<DiemThi, ?> thiSinh = root.join("thiSinh");
        Join<DiemThi, ?> phuongThuc = root.join("phuongThuc");
        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));
        cq.select(root).where(preds.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(thiSinh.get("ten")), cb.asc(thiSinh.get("ho")));
        TypedQuery<DiemThi> q = em().createQuery(cq);
        q.setFirstResult((page - 1) * pageSize);
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    public List<Object[]> thongKeDiemByPhuongThucMon(Short phuongthucId, Integer monId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<DiemThi> root = cq.from(DiemThi.class);
        Join<DiemThi, ?> dct = root.join("danhSachDiemChiTiet");
        Join<DiemThi, ?> phuongThuc = root.join("phuongThuc");
        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));
        if (monId != null) {
            Join<DiemThi, ?> mon = dct.join("mon");
            preds.add(cb.equal(mon.get("monId"), monId));
        }
        cq.multiselect(
            cb.avg(dct.get("diemSudung")),
            cb.min(dct.get("diemSudung")),
            cb.max(dct.get("diemSudung")),
            cb.count(dct)
        ).where(preds.toArray(new Predicate[0]));
        return em().createQuery(cq).getResultList();
    }

    public List<Object[]> thongKeDiemTheoMon(Short phuongthucId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<DiemThi> root = cq.from(DiemThi.class);
        Join<DiemThi, ?> dct = root.join("danhSachDiemChiTiet");
        Join<DiemThi, ?> phuongThuc = root.join("phuongThuc");
        Join<DiemThi, ?> mon = dct.join("mon");
        cq.multiselect(
            mon.get("maMon"),
            mon.get("tenMon"),
            cb.avg(dct.get("diemSudung")),
            cb.min(dct.get("diemSudung")),
            cb.max(dct.get("diemSudung")),
            cb.count(dct)
        );
        cq.where(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));
        cq.groupBy(mon.get("monId")).orderBy(cb.asc(mon.get("maMon")));
        return em().createQuery(cq).getResultList();
    }

    public DiemThi replaceForUniqueKey(DiemThi newEntity) {
        var em = em();
        em.getTransaction().begin();
        try {
            List<DiemThi> existingList = em.createQuery(
                            "select d from DiemThi d where d.thiSinh.thisinhId = :thisinhId and d.phuongThuc.phuongthucId = :phuongthucId and d.namTuyensinh = :nam",
                            DiemThi.class)
                    .setParameter("thisinhId", newEntity.getThiSinh().getThisinhId())
                    .setParameter("phuongthucId", newEntity.getPhuongThuc().getPhuongthucId())
                    .setParameter("nam", newEntity.getNamTuyensinh())
                    .setMaxResults(1)
                    .getResultList();

            if (!existingList.isEmpty()) {
                DiemThi existing = existingList.get(0);
                DiemThi managed = em.contains(existing) ? existing : em.merge(existing);
                em.remove(managed);
                em.flush();
            }

            em.persist(newEntity);
            em.getTransaction().commit();
            return newEntity;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public DiemThi findByIdWithDetails(Integer id) {
        List<DiemThi> list = em().createQuery(
                        "select distinct d " +
                                "from DiemThi d " +
                                "left join fetch d.thiSinh ts " +
                                "left join fetch d.phuongThuc pt " +
                                "left join fetch d.danhSachDiemChiTiet ct " +
                                "left join fetch ct.mon m " +
                                "where d.diemthiId = :id", DiemThi.class)
                .setParameter("id", id)
                .getResultList();

        return list.isEmpty() ? null : list.get(0);
    }

    public List<DiemThi> findByThiSinhIdWithDetails(Integer thisinhId) {
        return em().createQuery(
                        "select distinct d " +
                                "from DiemThi d " +
                                "left join fetch d.thiSinh ts " +
                                "left join fetch d.phuongThuc pt " +
                                "left join fetch d.danhSachDiemChiTiet ct " +
                                "left join fetch ct.mon m " +
                                "where ts.thisinhId = :thisinhId " +
                                "order by pt.phuongthucId, d.diemthiId", DiemThi.class)
                .setParameter("thisinhId", thisinhId)
                .getResultList();
    }


    public DiemThiChiTiet findChiTietById(Long id) {
        if (id == null) return null;
        List<DiemThiChiTiet> list = em().createQuery(
                        "select distinct ct " +
                                "from DiemThiChiTiet ct " +
                                "left join fetch ct.diemThi d " +
                                "left join fetch d.thiSinh ts " +
                                "left join fetch d.phuongThuc pt " +
                                "left join fetch ct.mon m " +
                                "where ct.diemthiCtId = :id", DiemThiChiTiet.class)
                .setParameter("id", id)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public DiemThiChiTiet findChiTietByDiemThiAndMon(Integer diemthiId, Integer monId) {
        if (diemthiId == null || monId == null) return null;
        List<DiemThiChiTiet> list = em().createQuery(
                        "select distinct ct " +
                                "from DiemThiChiTiet ct " +
                                "left join fetch ct.diemThi d " +
                                "left join fetch ct.mon m " +
                                "where d.diemthiId = :diemthiId and m.monId = :monId", DiemThiChiTiet.class)
                .setParameter("diemthiId", diemthiId)
                .setParameter("monId", monId)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public DiemThiChiTiet saveChiTiet(DiemThiChiTiet entity) {
        var em = em();
        em.getTransaction().begin();
        try {
            Integer diemthiId = entity.getDiemThi() != null ? entity.getDiemThi().getDiemthiId() : null;
            Integer monId = entity.getMon() != null ? entity.getMon().getMonId() : null;
            if (diemthiId == null || monId == null) {
                throw new IllegalArgumentException("Phiếu điểm và môn không được để trống.");
            }

            DiemThi diemThi = em.find(DiemThi.class, diemthiId);
            Mon mon = em.find(Mon.class, monId);
            if (diemThi == null) {
                throw new IllegalArgumentException("Không tìm thấy phiếu điểm ID " + diemthiId);
            }
            if (mon == null) {
                throw new IllegalArgumentException("Không tìm thấy môn ID " + monId);
            }

            entity.setDiemThi(diemThi);
            entity.setMon(mon);
            em.persist(entity);
            em.getTransaction().commit();
            return entity;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void updateChiTiet(DiemThiChiTiet entity) {
        var em = em();
        em.getTransaction().begin();
        try {
            Long id = entity.getDiemthiCtId();
            Integer monId = entity.getMon() != null ? entity.getMon().getMonId() : null;
            if (id == null || monId == null) {
                throw new IllegalArgumentException("Chi tiết điểm và môn không được để trống.");
            }

            DiemThiChiTiet managed = em.find(DiemThiChiTiet.class, id);
            Mon mon = em.find(Mon.class, monId);
            if (managed == null) {
                throw new IllegalArgumentException("Không tìm thấy chi tiết điểm ID " + id);
            }
            if (mon == null) {
                throw new IllegalArgumentException("Không tìm thấy môn ID " + monId);
            }

            managed.setMon(mon);
            managed.setDiemGoc(entity.getDiemGoc());
            managed.setDiemQuydoi(entity.getDiemQuydoi());
            managed.setDiemSudung(entity.getDiemSudung());
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void deleteChiTiet(DiemThiChiTiet entity) {
        var em = em();
        em.getTransaction().begin();
        try {
            em.remove(em.contains(entity) ? entity : em.merge(entity));
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public List<DiemThi> searchByCccdOrSoBaoDanh(String keyword, Short phuongthucId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<DiemThi> cq = cb.createQuery(DiemThi.class);
        Root<DiemThi> root = cq.from(DiemThi.class);
        Join<DiemThi, ?> thiSinh = root.join("thiSinh");
        Join<DiemThi, ?> phuongThuc = root.join("phuongThuc");
        String kw = "%" + keyword + "%";
        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.or(
            cb.like(thiSinh.get("cccd"), kw),
            cb.like(root.get("sobaodanh"), kw),
            cb.like(thiSinh.get("ho"), kw),
            cb.like(thiSinh.get("ten"), kw)
        ));
        if (phuongthucId != null) {
            preds.add(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));
        }
        cq.select(root).where(preds.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(thiSinh.get("ten")), cb.asc(thiSinh.get("ho")));
        return em().createQuery(cq).getResultList();
    }

    public List<DiemThi> findPage(int page, int pageSize) {
        return em().createQuery(
                        "select d from DiemThi d " +
                                "left join d.thiSinh ts " +
                                "left join d.phuongThuc pt " +
                                "order by ts.ten, ts.ho, pt.phuongthucId, d.diemthiId",
                        DiemThi.class)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public long countAll() {
        return count();
    }

    public long countByPhuongThuc(Short phuongthucId) {
        if (phuongthucId == null) return countAll();
        return em().createQuery(
                        "select count(d) from DiemThi d join d.phuongThuc pt where pt.phuongthucId = :ptId",
                        Long.class)
                .setParameter("ptId", phuongthucId)
                .getSingleResult();
    }

    public List<DiemThi> searchByCccdOrSoBaoDanhPage(String keyword, Short phuongthucId, int page, int pageSize) {
        String kw = "%" + normalizeKeyword(keyword) + "%";
        String filterPt = phuongthucId != null ? " and pt.phuongthucId = :ptId " : " ";
        javax.persistence.TypedQuery<DiemThi> query = em().createQuery(
                        "select d from DiemThi d " +
                                "left join d.thiSinh ts " +
                                "left join d.phuongThuc pt " +
                                "where (lower(coalesce(ts.cccd, '')) like :kw " +
                                "or lower(coalesce(d.sobaodanh, '')) like :kw " +
                                "or lower(coalesce(ts.ho, '')) like :kw " +
                                "or lower(coalesce(ts.ten, '')) like :kw) " +
                                filterPt +
                                "order by ts.ten, ts.ho, pt.phuongthucId, d.diemthiId",
                        DiemThi.class)
                .setParameter("kw", kw)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize);
        if (phuongthucId != null) {
            query.setParameter("ptId", phuongthucId);
        }
        return query.getResultList();
    }

    public long countSearchByCccdOrSoBaoDanh(String keyword, Short phuongthucId) {
        String kw = "%" + normalizeKeyword(keyword) + "%";
        String filterPt = phuongthucId != null ? " and pt.phuongthucId = :ptId " : " ";
        javax.persistence.TypedQuery<Long> query = em().createQuery(
                        "select count(d) from DiemThi d " +
                                "left join d.thiSinh ts " +
                                "left join d.phuongThuc pt " +
                                "where (lower(coalesce(ts.cccd, '')) like :kw " +
                                "or lower(coalesce(d.sobaodanh, '')) like :kw " +
                                "or lower(coalesce(ts.ho, '')) like :kw " +
                                "or lower(coalesce(ts.ten, '')) like :kw) " +
                                filterPt,
                        Long.class)
                .setParameter("kw", kw);
        if (phuongthucId != null) {
            query.setParameter("ptId", phuongthucId);
        }
        return query.getSingleResult();
    }



    public List<DiemThi> searchPageByField(String field, String keyword, int page, int pageSize) {
        javax.persistence.TypedQuery<DiemThi> query = buildSearchByFieldQuery(field, keyword, false);
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
        String f = normalizeField(field);
        String raw = keyword == null ? "" : keyword.trim();
        String low = raw.toLowerCase();

        String select = countOnly ? "select count(d) " : "select d ";
        String jpql = select +
                "from DiemThi d " +
                "left join d.thiSinh ts " +
                "left join d.phuongThuc pt ";

        String where;
        boolean like = false;
        switch (f) {
            case "ID":
                where = "d.diemthiId = :id";
                break;
            case "CCCD":
                where = "lower(coalesce(ts.cccd, '')) = :text";
                break;
            case "SBD":
                where = "lower(coalesce(d.sobaodanh, '')) = :text";
                break;
            case "MAPT":
                where = "lower(coalesce(pt.maPhuongthuc, '')) = :text";
                break;
            case "NAM":
                where = "d.namTuyensinh = :nam";
                break;
            case "HOTEN":
                where = "lower(concat(concat(coalesce(ts.ho, ''), ' '), coalesce(ts.ten, ''))) like :kw";
                like = true;
                break;
            case "TENPT":
                where = "lower(coalesce(pt.tenPhuongthuc, '')) like :kw";
                like = true;
                break;
            case "GHICHU":
                where = "lower(coalesce(d.ghiChu, '')) like :kw";
                like = true;
                break;
            default:
                where = "lower(coalesce(ts.cccd, '')) = :text " +
                        "or lower(coalesce(d.sobaodanh, '')) = :text " +
                        "or lower(coalesce(pt.maPhuongthuc, '')) = :text " +
                        "or lower(concat(concat(coalesce(ts.ho, ''), ' '), coalesce(ts.ten, ''))) like :kw " +
                        "or lower(coalesce(pt.tenPhuongthuc, '')) like :kw " +
                        "or lower(coalesce(d.ghiChu, '')) like :kw";
                like = true;
                break;
        }

        jpql += "where (" + where + ") ";
        if (!countOnly) {
            jpql += "order by ts.ten, ts.ho, pt.phuongthucId, d.diemthiId";
        }

        javax.persistence.TypedQuery<R> query;


        if (countOnly) {


            query = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, Long.class);


        } else {


            query = (javax.persistence.TypedQuery<R>) em().createQuery(jpql, DiemThi.class);


        }
        if (where.contains(":id")) {
            query.setParameter("id", parseIntegerOrNeverMatch(raw));
        }
        if (where.contains(":nam")) {
            query.setParameter("nam", parseShortOrNeverMatch(raw));
        }
        if (where.contains(":text")) {
            query.setParameter("text", low);
        }
        if (like || where.contains(":kw")) {
            query.setParameter("kw", "%" + low + "%");
        }
        return query;
    }

    private String normalizeField(String field) {
        return field == null ? "ALL" : field.trim().toUpperCase();
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
