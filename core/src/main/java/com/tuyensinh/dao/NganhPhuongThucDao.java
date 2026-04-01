package com.tuyensinh.dao;

import com.tuyensinh.entity.NganhPhuongThuc;
import org.hibernate.Session;
import java.util.List;

public class NganhPhuongThucDao extends BaseDao<NganhPhuongThuc> {

    @Override
    protected Class<NganhPhuongThuc> getEntityClass() {
        return NganhPhuongThuc.class;
    }

    @SuppressWarnings("unchecked")
    public List<NganhPhuongThuc> findByNganhId(Integer nganhId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM NganhPhuongThuc np WHERE np.nganh.nganhId = :nid ORDER BY np.phuongThuc.phuongthucId")
                .setParameter("nid", nganhId)
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<NganhPhuongThuc> findByPhuongThucId(Short phuongthucId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM NganhPhuongThuc np WHERE np.phuongThuc.phuongthucId = :ptid ORDER BY np.nganh.maNganh")
                .setParameter("ptid", phuongthucId)
                .getResultList();
        }
    }

    public NganhPhuongThuc findByNganhAndPhuongThuc(Integer nganhId, Short phuongthucId) {
        try (Session session = getSession()) {
            @SuppressWarnings("unchecked")
            NganhPhuongThuc np = (NganhPhuongThuc) session.createQuery(
                "FROM NganhPhuongThuc np WHERE np.nganh.nganhId = :nid AND np.phuongThuc.phuongthucId = :ptid")
                .setParameter("nid", nganhId)
                .setParameter("ptid", phuongthucId)
                .setMaxResults(1)
                .uniqueResult();
            return np;
        }
    }

    @SuppressWarnings("unchecked")
    public List<NganhPhuongThuc> findAll() {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM NganhPhuongThuc np ORDER BY np.nganh.maNganh, np.phuongThuc.phuongthucId")
                .getResultList();
        }
    }
}
