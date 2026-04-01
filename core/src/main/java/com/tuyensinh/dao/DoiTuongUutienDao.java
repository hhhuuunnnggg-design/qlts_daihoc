package com.tuyensinh.dao;

import com.tuyensinh.entity.DoiTuongUutien;
import org.hibernate.Session;
import java.util.List;

public class DoiTuongUutienDao extends BaseDao<DoiTuongUutien> {

    @Override
    protected Class<DoiTuongUutien> getEntityClass() {
        return DoiTuongUutien.class;
    }

    @SuppressWarnings("unchecked")
    public List<DoiTuongUutien> findAll() {
        try (Session session = getSession()) {
            return session.createQuery("FROM DoiTuongUutien ORDER BY maDoituong").getResultList();
        }
    }
}
