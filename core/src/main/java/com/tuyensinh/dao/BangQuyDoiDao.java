package com.tuyensinh.dao;

import com.tuyensinh.dao.InterfaceDao.IBangQuyDoiDao;
import com.tuyensinh.entity.BangQuyDoi;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.JoinType;
import java.util.Comparator;

public class BangQuyDoiDao extends BaseDao<BangQuyDoi> implements IBangQuyDoiDao {

    @Override
    protected Class<BangQuyDoi> getEntityClass() {
        return BangQuyDoi.class;
    }

    public Optional<BangQuyDoi> findByMa(String maQuydoi) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<BangQuyDoi> cq = cb.createQuery(BangQuyDoi.class);
        Root<BangQuyDoi> root = cq.from(BangQuyDoi.class);
        cq.select(root).where(cb.equal(root.get("maQuydoi"), maQuydoi));
        List<BangQuyDoi> list = em().createQuery(cq).setMaxResults(1).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<BangQuyDoi> findByPhuongThuc(Short phuongthucId) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<BangQuyDoi> cq = cb.createQuery(BangQuyDoi.class);
        Root<BangQuyDoi> root = cq.from(BangQuyDoi.class);
        Join<BangQuyDoi, ?> phuongThuc = root.join("phuongThuc");
        cq.select(root).where(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));
        cq.orderBy(cb.asc(root.get("diemTu")));
        return em().createQuery(cq).getResultList();
    }

    public List<BangQuyDoi> search(String keyword) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<BangQuyDoi> cq = cb.createQuery(BangQuyDoi.class);
        Root<BangQuyDoi> root = cq.from(BangQuyDoi.class);
        Join<BangQuyDoi, ?> phuongThuc = root.join("phuongThuc");
        String kw = "%" + keyword + "%";
        Predicate p1 = cb.like(root.get("maQuydoi"), kw);
        Predicate p2 = cb.like(phuongThuc.get("maPhuongthuc"), kw);
        cq.select(root).where(cb.or(p1, p2));
        Join<BangQuyDoi, ?> pt2 = root.join("phuongThuc");
        cq.orderBy(cb.asc(pt2.get("phuongthucId")), cb.asc(root.get("diemTu")));
        return em().createQuery(cq).getResultList();
    }

    public List<BangQuyDoi> findAll() {
        CriteriaBuilder cb = cb();
        CriteriaQuery<BangQuyDoi> cq = cb.createQuery(BangQuyDoi.class);
        Root<BangQuyDoi> root = cq.from(BangQuyDoi.class);
        Join<BangQuyDoi, ?> phuongThuc = root.join("phuongThuc");
        cq.select(root).orderBy(cb.asc(phuongThuc.get("phuongthucId")), cb.asc(root.get("diemTu")));
        return em().createQuery(cq).getResultList();
    }

    public BangQuyDoi quyDoiDiem(Short phuongthucId, Integer tohopId, Integer monId, BigDecimal diemGoc) {
        CriteriaBuilder cb = cb();
        CriteriaQuery<BangQuyDoi> cq = cb.createQuery(BangQuyDoi.class);
        Root<BangQuyDoi> root = cq.from(BangQuyDoi.class);

        Join<BangQuyDoi, ?> phuongThuc = root.join("phuongThuc");
        Join<BangQuyDoi, ?> toHopJoin = root.join("toHop", JoinType.LEFT);
        Join<BangQuyDoi, ?> monJoin = root.join("mon", JoinType.LEFT);

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(phuongThuc.get("phuongthucId"), phuongthucId));

        // Nguon tai lieu la (a < x <= b), nen lower bound phai la exclusive
        preds.add(cb.lessThan(root.get("diemTu"), diemGoc));
        preds.add(cb.greaterThanOrEqualTo(root.get("diemDen"), diemGoc));

        // toHop: uu tien match dung, neu khong co thi cho phep ban ghi toHop null
        if (tohopId != null) {
            preds.add(
                    cb.or(
                            cb.equal(toHopJoin.get("tohopId"), tohopId),
                            cb.isNull(root.get("toHop"))
                    )
            );
        } else {
            preds.add(cb.isNull(root.get("toHop")));
        }

        // mon: uu tien match dung, neu khong co thi cho phep ban ghi mon null
        if (monId != null) {
            preds.add(
                    cb.or(
                            cb.equal(monJoin.get("monId"), monId),
                            cb.isNull(root.get("mon"))
                    )
            );
        } else {
            preds.add(cb.isNull(root.get("mon")));
        }

        cq.select(root).where(preds.toArray(new Predicate[0]));

        List<BangQuyDoi> list = em().createQuery(cq).getResultList();
        if (list.isEmpty()) return null;

        list.sort(
                Comparator
                        .comparingInt((BangQuyDoi b) -> specificityScore(b, tohopId, monId))
                        .reversed()
                        .thenComparing(
                                BangQuyDoi::getDiemTu,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
        );

        return list.get(0);
    }

    private int specificityScore(BangQuyDoi b, Integer tohopId, Integer monId) {
        int score = 0;

        if (tohopId != null) {
            if (b.getToHop() != null && tohopId.equals(b.getToHop().getTohopId())) {
                score += 10;
            } else if (b.getToHop() == null) {
                score += 0;
            } else {
                return -1000;
            }
        } else if (b.getToHop() == null) {
            score += 1;
        }

        if (monId != null) {
            if (b.getMon() != null && monId.equals(b.getMon().getMonId())) {
                score += 5;
            } else if (b.getMon() == null) {
                score += 0;
            } else {
                return -1000;
            }
        } else if (b.getMon() == null) {
            score += 1;
        }

        return score;
    }
}
