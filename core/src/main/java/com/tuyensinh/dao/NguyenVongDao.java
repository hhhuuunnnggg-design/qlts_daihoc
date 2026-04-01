package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.INguyenVongDao;
import com.tuyensinh.entity.NguyenVong;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
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
}
