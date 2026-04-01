package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.IDiemCongDao;
import com.tuyensinh.entity.DiemCong;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DiemCongDao extends BaseDao<DiemCong> implements IDiemCongDao {

    @Override
    protected Class<DiemCong> getEntityClass() {
        return DiemCong.class;
    }

    public List<DiemCong> findByThiSinhId(Integer thisinhId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<DiemCong> cq = cb.createQuery(DiemCong.class);
        Root<DiemCong> root = cq.from(DiemCong.class);
        Join<DiemCong, ?> thiSinh = root.join("thiSinh");
        Join<DiemCong, ?> nganhToHop = root.join("nganhToHop");
        Join<DiemCong, ?> nganh = nganhToHop.join("nganh");
        cq.select(root).where(cb.equal(thiSinh.get("thisinhId"), thisinhId));
        cq.orderBy(cb.asc(nganh.get("maNganh")));
        return em().createQuery(cq).getResultList();
    }

    public Optional<DiemCong> findByThiSinhNganhToHopPhuongThuc(
            Integer thisinhId, Integer nganhToHopId, Short phuongthucId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<DiemCong> cq = cb.createQuery(DiemCong.class);
        Root<DiemCong> root = cq.from(DiemCong.class);
        Join<DiemCong, ?> thiSinh = root.join("thiSinh");
        Join<DiemCong, ?> nganhToHop = root.join("nganhToHop");
        Join<DiemCong, ?> phuongThuc = root.join("phuongThuc");
        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(thiSinh.get("thisinhId"), thisinhId));
        preds.add(cb.equal(nganhToHop.get("nganhTohopId"), nganhToHopId));
        preds.add(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));
        cq.select(root).where(preds.toArray(new Predicate[0]));
        List<DiemCong> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<DiemCong> findAll() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<DiemCong> cq = cb.createQuery(DiemCong.class);
        Root<DiemCong> root = cq.from(DiemCong.class);
        Join<DiemCong, ?> thiSinh = root.join("thiSinh");
        cq.select(root).orderBy(cb.asc(thiSinh.get("ten")), cb.asc(thiSinh.get("ho")));
        return em().createQuery(cq).getResultList();
    }

    public List<DiemCong> findByPhuongThuc(Short phuongthucId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<DiemCong> cq = cb.createQuery(DiemCong.class);
        Root<DiemCong> root = cq.from(DiemCong.class);
        Join<DiemCong, ?> phuongThuc = root.join("phuongThuc");
        Join<DiemCong, ?> thiSinh = root.join("thiSinh");
        cq.select(root).where(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));
        cq.orderBy(cb.asc(thiSinh.get("ten")));
        return em().createQuery(cq).getResultList();
    }
}
