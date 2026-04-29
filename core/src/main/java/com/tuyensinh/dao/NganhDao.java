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

}