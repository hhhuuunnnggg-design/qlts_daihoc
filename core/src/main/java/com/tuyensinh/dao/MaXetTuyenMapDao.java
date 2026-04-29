package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.IMaXetTuyenMapDao;
import com.tuyensinh.entity.MaXetTuyenMap;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaXetTuyenMapDao extends BaseDao<MaXetTuyenMap> implements IMaXetTuyenMapDao {

    @Override
    protected Class<MaXetTuyenMap> getEntityClass() {
        return MaXetTuyenMap.class;
    }

    @Override
    public List<MaXetTuyenMap> findAll() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<MaXetTuyenMap> cq = cb.createQuery(MaXetTuyenMap.class);
        Root<MaXetTuyenMap> root = cq.from(MaXetTuyenMap.class);

        cq.select(root).orderBy(
                cb.asc(root.get("maXetTuyen")),
                cb.asc(root.get("phuongThuc").get("phuongthucId")),
                cb.asc(root.get("maTohopNguon"))
        );

        return em().createQuery(cq).getResultList();
    }

    public List<MaXetTuyenMap> search(String keyword) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<MaXetTuyenMap> cq = cb.createQuery(MaXetTuyenMap.class);
        Root<MaXetTuyenMap> root = cq.from(MaXetTuyenMap.class);

        Join<Object, Object> nganh = root.join("nganh", JoinType.LEFT);
        Join<Object, Object> pt = root.join("phuongThuc", JoinType.LEFT);
        Join<Object, Object> nt = root.join("nganhToHop", JoinType.LEFT);
        Join<Object, Object> toHop = nt.join("toHop", JoinType.LEFT);

        String kw = "%" + keyword.trim().toUpperCase() + "%";

        Predicate p = cb.or(
                cb.like(cb.upper(root.get("maXetTuyen")), kw),
                cb.like(cb.upper(root.get("tenChuongTrinh")), kw),
                cb.like(cb.upper(root.get("maTohopNguon")), kw),
                cb.like(cb.upper(root.get("ghiChu")), kw),
                cb.like(cb.upper(nganh.get("maNganh")), kw),
                cb.like(cb.upper(nganh.get("tenNganh")), kw),
                cb.like(cb.upper(pt.get("maPhuongthuc")), kw),
                cb.like(cb.upper(pt.get("tenPhuongthuc")), kw),
                cb.like(cb.upper(toHop.get("maTohop")), kw)
        );

        cq.select(root).where(p).orderBy(
                cb.asc(root.get("maXetTuyen")),
                cb.asc(root.get("phuongThuc").get("phuongthucId")),
                cb.asc(root.get("maTohopNguon"))
        );

        return em().createQuery(cq).getResultList();
    }

    @Override
    public List<MaXetTuyenMap> findByMaXetTuyen(String maXetTuyen) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<MaXetTuyenMap> cq = cb.createQuery(MaXetTuyenMap.class);
        Root<MaXetTuyenMap> root = cq.from(MaXetTuyenMap.class);

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(cb.upper(root.get("maXetTuyen")), maXetTuyen.trim().toUpperCase()));

        cq.select(root).where(preds.toArray(new Predicate[0])).orderBy(
                cb.asc(root.get("phuongThuc").get("phuongthucId")),
                cb.asc(root.get("maTohopNguon"))
        );

        return em().createQuery(cq).getResultList();
    }

    @Override
    public Optional<MaXetTuyenMap> findExact(String maXetTuyen, Short phuongthucId, String maTohopNguon) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<MaXetTuyenMap> cq = cb.createQuery(MaXetTuyenMap.class);
        Root<MaXetTuyenMap> root = cq.from(MaXetTuyenMap.class);

        String maTh = maTohopNguon == null ? "" : maTohopNguon.trim().toUpperCase();

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(cb.upper(root.get("maXetTuyen")), maXetTuyen.trim().toUpperCase()));
        preds.add(cb.equal(root.get("phuongThuc").get("phuongthucId"), phuongthucId));
        preds.add(cb.equal(cb.upper(root.get("maTohopNguon")), maTh));

        cq.select(root).where(preds.toArray(new Predicate[0]));

        List<MaXetTuyenMap> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<MaXetTuyenMap> findByNganhId(Integer nganhId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<MaXetTuyenMap> cq = cb.createQuery(MaXetTuyenMap.class);
        Root<MaXetTuyenMap> root = cq.from(MaXetTuyenMap.class);

        cq.select(root)
                .where(cb.equal(root.get("nganh").get("nganhId"), nganhId))
                .orderBy(
                        cb.asc(root.get("maXetTuyen")),
                        cb.asc(root.get("phuongThuc").get("phuongthucId")),
                        cb.asc(root.get("maTohopNguon"))
                );

        return em().createQuery(cq).getResultList();
    }

    /**
     * Load day du nganh, phuong thuc, nganh-to-hop, to-hop va he so mon.
     * Dung trong TinhDiemService de so sanh THPT/VSAT/DGNL ma khong bi proxy detached.
     */
    public MaXetTuyenMap findByIdWithDetails(Integer id) {
        if (id == null) return null;
        List<MaXetTuyenMap> list = em().createQuery(
                        "select distinct m " +
                                "from MaXetTuyenMap m " +
                                "left join fetch m.nganh n " +
                                "left join fetch m.phuongThuc pt " +
                                "left join fetch m.nganhToHop nth " +
                                "left join fetch nth.toHop th " +
                                "left join fetch nth.danhSachNganhToHopMon nthm " +
                                "left join fetch nthm.mon mon " +
                                "where m.maXettuyenId = :id", MaXetTuyenMap.class)
                .setParameter("id", id)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public List<MaXetTuyenMap> findByMaXetTuyenWithDetails(String maXetTuyen) {
        if (maXetTuyen == null || maXetTuyen.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return em().createQuery(
                        "select distinct m " +
                                "from MaXetTuyenMap m " +
                                "left join fetch m.nganh n " +
                                "left join fetch m.phuongThuc pt " +
                                "left join fetch m.nganhToHop nth " +
                                "left join fetch nth.toHop th " +
                                "left join fetch nth.danhSachNganhToHopMon nthm " +
                                "left join fetch nthm.mon mon " +
                                "where upper(m.maXetTuyen) = :ma " +
                                "order by pt.phuongthucId, m.maTohopNguon", MaXetTuyenMap.class)
                .setParameter("ma", maXetTuyen.trim().toUpperCase())
                .getResultList();
    }

    public void deleteAll() {
        var entityManager = em();
        entityManager.getTransaction().begin();
        try {
            entityManager.createQuery("delete from MaXetTuyenMap").executeUpdate();
            entityManager.getTransaction().commit();
        } catch (RuntimeException e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }
}