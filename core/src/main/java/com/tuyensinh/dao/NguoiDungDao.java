package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.INguoiDungDao;
import com.tuyensinh.entity.NguoiDung;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

public class NguoiDungDao extends BaseDao<NguoiDung> implements INguoiDungDao {

    @Override
    protected Class<NguoiDung> getEntityClass() {
        return NguoiDung.class;
    }

    public Optional<NguoiDung> findByUsername(String username) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NguoiDung> cq = cb.createQuery(NguoiDung.class);
        Root<NguoiDung> root = cq.from(NguoiDung.class);
        cq.select(root).where(cb.equal(root.get("username"), username));
        List<NguoiDung> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<NguoiDung> findByEmail(String email) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NguoiDung> cq = cb.createQuery(NguoiDung.class);
        Root<NguoiDung> root = cq.from(NguoiDung.class);
        cq.select(root).where(cb.equal(root.get("email"), email));
        List<NguoiDung> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<NguoiDung> findByVaiTro(Short vaitroId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NguoiDung> cq = cb.createQuery(NguoiDung.class);
        Root<NguoiDung> root = cq.from(NguoiDung.class);
        Join<NguoiDung, ?> vaiTro = root.join("vaiTro");
        cq.select(root).where(
            cb.equal(vaiTro.get("vaitroId"), vaitroId)
        );
        cq.orderBy(cb.asc(root.get("username")));
        return em().createQuery(cq).getResultList();
    }

    public List<NguoiDung> findActiveUsers() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NguoiDung> cq = cb.createQuery(NguoiDung.class);
        Root<NguoiDung> root = cq.from(NguoiDung.class);
        cq.select(root).where(cb.equal(root.get("isActive"), true));
        cq.orderBy(cb.asc(root.get("username")));
        return em().createQuery(cq).getResultList();
    }

    public List<NguoiDung> searchByUsernameOrHoTen(String keyword) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NguoiDung> cq = cb.createQuery(NguoiDung.class);
        Root<NguoiDung> root = cq.from(NguoiDung.class);
        String kw = "%" + keyword + "%";
        Predicate p1 = cb.like(root.get("username"), kw);
        Predicate p2 = cb.like(root.get("hoTen"), kw);
        cq.select(root).where(cb.or(p1, p2));
        cq.orderBy(cb.asc(root.get("username")));
        return em().createQuery(cq).getResultList();
    }

    public List<NguoiDung> findByPage(int page, int pageSize) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<NguoiDung> cq = cb.createQuery(NguoiDung.class);
        Root<NguoiDung> root = cq.from(NguoiDung.class);
        cq.select(root).orderBy(cb.asc(root.get("username")));
        TypedQuery<NguoiDung> q = em().createQuery(cq);
        q.setFirstResult((page - 1) * pageSize);
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    public long countAll() {
        return count();
    }

    @Override
    public NguoiDung save(NguoiDung nd) {
        if (findByUsername(nd.getUsername()).isPresent()) {
            throw new RuntimeException("Username '" + nd.getUsername() + "' da ton tai!");
        }
        if (nd.getEmail() != null && !nd.getEmail().isEmpty()) {
            if (findByEmail(nd.getEmail()).isPresent()) {
                throw new RuntimeException("Email '" + nd.getEmail() + "' da duoc su dung!");
            }
        }
        if (nd.getVaiTro() == null || nd.getVaiTro().getVaitroId() == null) {
            throw new RuntimeException("Vai tro khong hop le!");
        }
        return super.save(nd);
    }

    @Override
    public void update(NguoiDung nd) {
        if (nd.getEmail() != null && !nd.getEmail().isEmpty()) {
            Optional<NguoiDung> existing = findByEmail(nd.getEmail());
            if (existing.isPresent() && !existing.get().getNguoidungId().equals(nd.getNguoidungId())) {
                throw new RuntimeException("Email '" + nd.getEmail() + "' da duoc su dung boi tai khoan khac!");
            }
        }
        super.update(nd);
    }

    public void updatePassword(NguoiDung nd, String newPasswordHash) {
        EntityManager em = em();
        em.getTransaction().begin();
        try {
            nd.setPasswordHash(newPasswordHash);
            em.merge(nd);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void toggleActive(NguoiDung nd) {
        EntityManager em = em();
        em.getTransaction().begin();
        try {
            nd.setIsActive(!nd.getIsActive());
            em.merge(nd);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }
}
