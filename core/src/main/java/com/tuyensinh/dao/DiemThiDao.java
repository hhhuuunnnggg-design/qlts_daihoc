package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.IDiemThiDao;
import com.tuyensinh.entity.DiemThi;
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
}
