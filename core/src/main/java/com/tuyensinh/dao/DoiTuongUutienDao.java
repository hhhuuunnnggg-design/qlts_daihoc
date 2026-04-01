package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.IDoiTuongUutienDao;
import com.tuyensinh.entity.DoiTuongUutien;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class DoiTuongUutienDao extends BaseDao<DoiTuongUutien> implements IDoiTuongUutienDao {

    @Override
    protected Class<DoiTuongUutien> getEntityClass() {
        return DoiTuongUutien.class;
    }

    public List<DoiTuongUutien> findAll() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<DoiTuongUutien> cq = cb.createQuery(DoiTuongUutien.class);
        Root<DoiTuongUutien> root = cq.from(DoiTuongUutien.class);
        cq.select(root).orderBy(cb.asc(root.get("maDoituong")));
        return em().createQuery(cq).getResultList();
    }
}
