package com.tuyensinh.dao;

import com.tuyensinh.entity.VaiTro;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class VaiTroDao extends BaseDao<VaiTro> {

    @Override
    protected Class<VaiTro> getEntityClass() {
        return VaiTro.class;
    }

    public List<VaiTro> findAll() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<VaiTro> cq = cb.createQuery(VaiTro.class);
        Root<VaiTro> root = cq.from(VaiTro.class);
        cq.select(root).orderBy(cb.asc(root.get("vaitroId")));
        return em().createQuery(cq).getResultList();
    }

    public VaiTro findByMa(String maVaitro) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<VaiTro> cq = cb.createQuery(VaiTro.class);
        Root<VaiTro> root = cq.from(VaiTro.class);
        cq.select(root).where(cb.equal(root.get("maVaitro"), maVaitro));
        List<VaiTro> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public VaiTro findById(Short id) {
        return em().find(VaiTro.class, id);
    }
}
