package com.tuyensinh.dao;

import com.tuyensinh.entity.VaiTro;
import org.hibernate.Session;
import java.util.List;

public class VaiTroDao extends BaseDao<VaiTro> {

    @Override
    protected Class<VaiTro> getEntityClass() {
        return VaiTro.class;
    }

    @SuppressWarnings("unchecked")
    public List<VaiTro> findAll() {
        try (Session session = getSession()) {
            return session.createQuery("FROM VaiTro ORDER BY vaitroId").getResultList();
        }
    }

    public VaiTro findByMa(String maVaitro) {
        try (Session session = getSession()) {
            return (VaiTro) session.createQuery(
                "FROM VaiTro vt WHERE vt.maVaitro = :ma")
                .setParameter("ma", maVaitro)
                .setMaxResults(1)
                .uniqueResult();
        }
    }

    public VaiTro findById(Short id) {
        try (Session session = getSession()) {
            return session.get(VaiTro.class, id);
        }
    }
}
