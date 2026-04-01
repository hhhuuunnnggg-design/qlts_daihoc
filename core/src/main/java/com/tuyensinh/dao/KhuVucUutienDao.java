package com.tuyensinh.dao;

import com.tuyensinh.entity.KhuVucUutien;
import org.hibernate.Session;
import java.util.List;

public class KhuVucUutienDao extends BaseDao<KhuVucUutien> {

    @Override
    protected Class<KhuVucUutien> getEntityClass() {
        return KhuVucUutien.class;
    }

    @SuppressWarnings("unchecked")
    public List<KhuVucUutien> findAll() {
        try (Session session = getSession()) {
            return session.createQuery("FROM KhuVucUutien ORDER BY maKhuvuc").getResultList();
        }
    }
}
