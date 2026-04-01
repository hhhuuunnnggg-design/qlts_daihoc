package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.IKhuVucUutienDao;
import com.tuyensinh.entity.KhuVucUutien;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class KhuVucUutienDao extends BaseDao<KhuVucUutien> implements IKhuVucUutienDao {

    @Override
    protected Class<KhuVucUutien> getEntityClass() {
        return KhuVucUutien.class;
    }

    public List<KhuVucUutien> findAll() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<KhuVucUutien> cq = cb.createQuery(KhuVucUutien.class);
        Root<KhuVucUutien> root = cq.from(KhuVucUutien.class);
        cq.select(root).orderBy(cb.asc(root.get("maKhuvuc")));
        return em().createQuery(cq).getResultList();
    }
}
