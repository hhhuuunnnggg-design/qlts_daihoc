package com.tuyensinh;

import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import com.tuyensinh.util.*;
import org.hibernate.Session;

public class TestAddUser {
    public static void main(String[] args) {
        try {
            System.out.println("=== TEST: Them nguoi dung moi ===\n");

            // Init Hibernate
            Session session = com.tuyensinh.util.HibernateUtil.getSession();
            System.out.println("Hibernate OK\n");

            // Lay role USER
            VaiTroService vtService = new VaiTroService();
            VaiTro userRole = vtService.findByMa("USER");
            System.out.println("USER role: " + (userRole != null ? userRole.getVaitroId() + " - " + userRole.getMaVaitro() : "NULL"));

            if (userRole == null) {
                System.out.println("LOI: Khong tim thay role USER!");
                return;
            }

            // Tao user moi
            NguoiDung nd = new NguoiDung();
            nd.setUsername("testuser999");
            nd.setPasswordHash(PasswordUtil.hashPassword("test123"));
            nd.setHoTen("Test User");
            nd.setEmail("test999@tuyensinh.edu.vn");
            nd.setVaiTro(userRole);
            nd.setIsActive(true);

            System.out.println("\nThong tin nguoi dung:");
            System.out.println("  username: " + nd.getUsername());
            System.out.println("  email: " + nd.getEmail());
            System.out.println("  hoTen: " + nd.getHoTen());
            System.out.println("  vaiTro: " + nd.getVaiTro().getMaVaitro() + " (id=" + nd.getVaiTro().getVaitroId() + ")");
            System.out.println("  isActive: " + nd.getIsActive());
            System.out.println();

            // Goi service de luu
            NguoiDungService service = new NguoiDungService();
            System.out.println("Dang goi service.save()...");

            NguoiDung saved = service.save(nd);
            System.out.println("THEM THANH CONG!");
            System.out.println("  ID moi: " + saved.getNguoidungId());
            System.out.println("  Username: " + saved.getUsername());

            // Cleanup
            System.out.println("\nXoa user test...");
            service.delete(saved);
            System.out.println("Xoa thanh cong!");

            System.out.println("\n=== KET QUA: CRUD hoat dong tot! ===");

        } catch (Exception e) {
            System.err.println("LOI XAY RA:");
            System.err.println("  Type: " + e.getClass().getName());
            System.err.println("  Message: " + e.getMessage());
            System.err.println("\nStack trace:");
            e.printStackTrace();
        }
    }
}
