package com.tuyensinh.dao;

import com.tuyensinh.entity.BangQuyDoi;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

public class BangQuyDoiDao extends BaseDao<BangQuyDoi> {

    @Override
    protected Class<BangQuyDoi> getEntityClass() {
        return BangQuyDoi.class;
    }

    public Optional<BangQuyDoi> findByMa(String maQuydoi) {
        try (Session session = getSession()) {
            @SuppressWarnings("unchecked")
            BangQuyDoi bqd = (BangQuyDoi) session.createQuery(
                "FROM BangQuyDoi bqd WHERE bqd.maQuydoi = :ma")
                .setParameter("ma", maQuydoi)
                .setMaxResults(1)
                .uniqueResult();
            return Optional.ofNullable(bqd);
        }
    }

    @SuppressWarnings("unchecked")
    public List<BangQuyDoi> findByPhuongThuc(Short phuongthucId) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM BangQuyDoi bqd WHERE bqd.phuongThuc.phuongthucId = :ptid ORDER BY bqd.diemTu")
                .setParameter("ptid", phuongthucId)
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<BangQuyDoi> search(String keyword) {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM BangQuyDoi bqd WHERE bqd.maQuydoi LIKE :kw OR bqd.phuongThuc.maPhuongthuc LIKE :kw ORDER BY bqd.phuongThuc.phuongthucId, bqd.diemTu")
                .setParameter("kw", "%" + keyword + "%")
                .getResultList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<BangQuyDoi> findAll() {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM BangQuyDoi bqd ORDER BY bqd.phuongThuc.phuongthucId, bqd.diemTu")
                .getResultList();
        }
    }

    public BangQuyDoi quyDoiDiem(Short phuongthucId, Integer tohopId, Integer monId, java.math.BigDecimal diemGoc) {
        try (Session session = getSession()) {
            @SuppressWarnings("unchecked")
            BangQuyDoi bqd = (BangQuyDoi) session.createQuery(
                "FROM BangQuyDoi bqd WHERE bqd.phuongThuc.phuongthucId = :ptid " +
                "AND (:tid IS NULL OR bqd.toHop.tohopId = :tid) " +
                "AND (:mid IS NULL OR bqd.mon.monId = :mid) " +
                "AND bqd.diemTu <= :diem AND bqd.diemDen >= :diem " +
                "ORDER BY bqd.diemTu DESC")
                .setParameter("ptid", phuongthucId)
                .setParameter("tid", tohopId)
                .setParameter("mid", monId)
                .setParameter("diem", diemGoc)
                .setMaxResults(1)
                .uniqueResult();
            return bqd;
        }
    }
}
