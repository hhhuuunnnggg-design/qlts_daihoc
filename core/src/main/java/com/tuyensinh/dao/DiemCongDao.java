package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.IDiemCongDao;
import com.tuyensinh.entity.DiemCong;

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

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase();
    }

}