package com.tuyensinh.util;

import com.tuyensinh.dao.NguoiDungDao;
import com.tuyensinh.dao.VaiTroDao;
import com.tuyensinh.entity.NguoiDung;
import com.tuyensinh.entity.VaiTro;
import org.hibernate.Session;
import org.hibernate.Transaction;

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
        // --- VaiTro ---
        VaiTro adminRole = getOrCreateVaiTro(session, "ADMIN", "Quan tri vien");
        getOrCreateVaiTro(session, "USER", "Nguoi dung");

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
}
