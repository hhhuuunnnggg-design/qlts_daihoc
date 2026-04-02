package com.tuyensinh.util;

import com.tuyensinh.dao.NguoiDungDao;
import com.tuyensinh.dao.VaiTroDao;
import com.tuyensinh.entity.DoiTuongUutien;
import com.tuyensinh.entity.KhuVucUutien;
import com.tuyensinh.entity.NguoiDung;
import com.tuyensinh.entity.VaiTro;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;

public class DataSeeder {

    private static boolean seeded = false;

    public static void seedIfNeeded() {
        if (seeded) return;

        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            seed(session);
            tx.commit();
            seeded = true;
            System.out.println("[DataSeeder] Du lieu khoi tao da duoc insert thanh cong!");
        } catch (Exception e) {
            tx.rollback();
            System.err.println("[DataSeeder] Loi khi insert du lieu: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private static void seed(Session session) {
        // --- Vai trò ---
        VaiTro adminRole = getOrCreateVaiTro(session, "ADMIN", "Quan tri vien");
        getOrCreateVaiTro(session, "USER", "Nguoi dung");

        // --- Khu Vực Ưu tiên ---
        getOrCreateKhuVucUutien(session, "KV1", "Khu vực 1",
                new BigDecimal("0.75"),
                "Vùng sâu, vùng xa, miền núi, hải đảo, vùng đặc biệt khó khăn");
        getOrCreateKhuVucUutien(session, "KV2-NT", "Khu vực 2 - Nông thôn",
                new BigDecimal("0.50"),
                "Khu vực nông thôn (không thuộc KV1)");
        getOrCreateKhuVucUutien(session, "KV2", "Khu vực 2",
                new BigDecimal("0.25"),
                "Thành phố trực thuộc tỉnh, thị xã");
        getOrCreateKhuVucUutien(session, "KV3", "Khu vực 3",
                BigDecimal.ZERO,
                "Quận nội thành của các thành phố lớn (Hà Nội, TP.HCM, v.v.)");

        // --- Đối tượng ưu tiên ---
        // Nhóm UT1 - cộng 2.0 điểm
        getOrCreateDoiTuongUutien(session, "UT1-01", "Con liệt sĩ",
                new BigDecimal("2.00"),
                "Con liệt sĩ, con thương binh nặng");
        getOrCreateDoiTuongUutien(session, "UT1-02", "Con thương binh nặng - bệnh binh",
                new BigDecimal("2.00"),
                "Con thương binh nặng, bệnh binh suy giảm ≥81%");
        getOrCreateDoiTuongUutien(session, "UT1-03", "Người dân tộc thiểu số - vùng đặc biệt khó khăn",
                new BigDecimal("2.00"),
                "Người dân tộc thiểu số ở vùng đặc biệt khó khăn");
        getOrCreateDoiTuongUutien(session, "UT1-04", "Người bị nhiễm chất độc hóa học",
                new BigDecimal("2.00"),
                "Người bị nhiễm chất độc hóa học nặng");
        getOrCreateDoiTuongUutien(session, "UT1-05", "Người khuyết tật nặng",
                new BigDecimal("2.00"),
                "Người khuyết tật nặng");

        // Nhóm UT2 - cộng 1.0 điểm
        getOrCreateDoiTuongUutien(session, "UT2-01", "Con thương binh - bệnh binh",
                new BigDecimal("1.00"),
                "Con thương binh, bệnh binh suy giảm <81%");
        getOrCreateDoiTuongUutien(session, "UT2-02", "Con người có công với cách mạng",
                new BigDecimal("1.00"),
                "Con của người có công với cách mạng");
        getOrCreateDoiTuongUutien(session, "UT2-03", "Người dân tộc thiểu số",
                new BigDecimal("1.00"),
                "Người dân tộc thiểu số (không thuộc UT1)");
        getOrCreateDoiTuongUutien(session, "UT2-04", "Người ở vùng khó khăn",
                new BigDecimal("1.00"),
                "Người ở vùng khó khăn (không thuộc diện đặc biệt khó khăn)");

        // Nhóm UT3 - không cộng điểm
        getOrCreateDoiTuongUutien(session, "UT3-00", "Không thuộc diện ưu tiên",
                BigDecimal.ZERO,
                "Không thuộc diện ưu tiên");

        // --- NguoiDung admin ---
        if (findByUsername(session, "admin") == null) {
            NguoiDung admin = new NguoiDung();
            admin.setUsername("admin");
            admin.setPasswordHash(PasswordUtil.hashPassword("admin123"));
            admin.setHoTen("Quan Tri Vien");
            admin.setEmail("admin@tuyensinh.edu.vn");
            admin.setVaiTro(adminRole);
            admin.setIsActive(true);
            session.persist(admin);
        }
    }

    private static VaiTro getOrCreateVaiTro(Session session, String maVaitro, String tenVaitro) {
        @SuppressWarnings("unchecked")
        VaiTro existing = (VaiTro) session
            .createQuery("FROM VaiTro vt WHERE vt.maVaitro = :ma")
            .setParameter("ma", maVaitro)
            .setMaxResults(1)
            .uniqueResult();
        if (existing != null) return existing;

        VaiTro vt = new VaiTro();
        vt.setMaVaitro(maVaitro);
        vt.setTenVaitro(tenVaitro);
        session.persist(vt);
        return vt;
    }

    private static NguoiDung findByUsername(Session session, String username) {
        @SuppressWarnings("unchecked")
        NguoiDung nd = (NguoiDung) session
            .createQuery("FROM NguoiDung nd WHERE nd.username = :u")
            .setParameter("u", username)
            .setMaxResults(1)
            .uniqueResult();
        return nd;
    }

    private static KhuVucUutien getOrCreateKhuVucUutien(Session session, String maKhuvuc,
            String tenKhuvuc, BigDecimal mucDiem, String ghiChu) {
        @SuppressWarnings("unchecked")
        KhuVucUutien existing = (KhuVucUutien) session
            .createQuery("FROM KhuVucUutien kv WHERE kv.maKhuvuc = :ma")
            .setParameter("ma", maKhuvuc)
            .setMaxResults(1)
            .uniqueResult();
        if (existing != null) return existing;

        KhuVucUutien kv = new KhuVucUutien();
        kv.setMaKhuvuc(maKhuvuc);
        kv.setTenKhuvuc(tenKhuvuc);
        kv.setMucDiem(mucDiem);
        kv.setGhiChu(ghiChu);
        session.persist(kv);
        return kv;
    }

    private static DoiTuongUutien getOrCreateDoiTuongUutien(Session session, String maDoituong,
            String tenDoituong, BigDecimal mucDiem, String ghiChu) {
        @SuppressWarnings("unchecked")
        DoiTuongUutien existing = (DoiTuongUutien) session
            .createQuery("FROM DoiTuongUutien dt WHERE dt.maDoituong = :ma")
            .setParameter("ma", maDoituong)
            .setMaxResults(1)
            .uniqueResult();
        if (existing != null) return existing;

        DoiTuongUutien dt = new DoiTuongUutien();
        dt.setMaDoituong(maDoituong);
        dt.setTenDoituong(tenDoituong);
        dt.setMucDiem(mucDiem);
        dt.setGhiChu(ghiChu);
        session.persist(dt);
        return dt;
    }
}
