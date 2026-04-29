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
                                        "    nv.ketQua = :ketQua, " +
                                        "    nv.ghiChu = :ghiChu " +
                                        "WHERE nv.nguyenvongId = :id")
                        .setParameter("diemThxt", nv.getDiemThxt())
                        .setParameter("diemCong", nv.getDiemCong())
                        .setParameter("diemUutien", nv.getDiemUutien())
                        .setParameter("diemXettuyen", nv.getDiemXettuyen())
                        .setParameter("phuongThucDiemTotNhat", nv.getPhuongThucDiemTotNhat())
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