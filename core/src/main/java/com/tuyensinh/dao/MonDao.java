package com.tuyensinh.dao;

import com.tuyensinh.entity.Mon;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

public class MonDao extends BaseDao<Mon> {

    @Override
    protected Class<Mon> getEntityClass() {
        return Mon.class;
    }

    public Optional<Mon> findByMa(String maMon) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<Mon> cq = cb.createQuery(Mon.class);
        Root<Mon> root = cq.from(Mon.class);
        cq.select(root).where(cb.equal(root.get("maMon"), maMon));
        List<Mon> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Mon> findByLoaiMon(String loaiMon) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<Mon> cq = cb.createQuery(Mon.class);
        Root<Mon> root = cq.from(Mon.class);
        cq.select(root).where(cb.equal(root.get("loaiMon"), loaiMon));
        cq.orderBy(cb.asc(root.get("maMon")));
        return em().createQuery(cq).getResultList();
    }

    public List<Mon> findNangKhieuMon() {
        return findByLoaiMon(Mon.LoaiMon.NANG_KHIEU);
    }

    public List<Mon> findDanhGiaNangLuc() {
        return findByLoaiMon(Mon.LoaiMon.DANH_GIA_NANG_LUC);
    }

    public List<Mon> findMonHoc() {
        return findByLoaiMon(Mon.LoaiMon.MON_HOC);
    }
}
