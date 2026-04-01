package com.tuyensinh;

import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import com.tuyensinh.util.*;
import org.hibernate.Session;

public class TestAdminFlow {
    public static void main(String[] args) {
        try {
            System.out.println("=== SIMULATE ADMIN APP FLOW ===\n");

            // Simulate what NguoiDungPanel does on startup
            System.out.println("1. Load VaiTro (NguoiDungPanel init)...");
            VaiTroService vtService = new VaiTroService();
            for (VaiTro vt : vtService.findAllVaiTro()) {
                System.out.println("   VaiTro: id=" + vt.getVaitroId() + ", ma=" + vt.getMaVaitro());
            }
            System.out.println("   OK\n");

            // Simulate what happens when user clicks "Them moi"
            System.out.println("2. Tao user moi (simulate dialog OK)...");

            // This is what NguoiDungPanel does - creates NguoiDungService fresh
            NguoiDungService ndService = new NguoiDungService();

            // Get the same USER role (but loaded by a DIFFERENT service/dao)
            VaiTroService vtService2 = new VaiTroService();  // FRESH service
            VaiTro userRole = vtService2.findByMa("USER");
            System.out.println("   USER role from fresh service: " + userRole.getVaitroId() + " - " + userRole.getMaVaitro());

            // Build entity (same as NguoiDungPanel.showAddDialog)
            NguoiDung nd = new NguoiDung();
            nd.setUsername("testuser99x");
            nd.setPasswordHash(PasswordUtil.hashPassword("test123"));
            nd.setHoTen("Test User 99");
            nd.setEmail("test99x@tuyensinh.edu.vn");
            nd.setVaiTro(userRole);   // role loaded by different service/dao
            nd.setIsActive(true);

            System.out.println("   Entity: username=" + nd.getUsername() + ", vaiTro.id=" + nd.getVaiTro().getVaitroId());

            // Save
            System.out.println("3. Calling ndService.save()...");
            NguoiDung saved = ndService.save(nd);
            System.out.println("   SUCCESS! ID=" + saved.getNguoidungId() + ", username=" + saved.getUsername());

            // Cleanup
            System.out.println("\n4. Cleanup...");
            ndService.delete(saved);
            System.out.println("   Deleted OK");

            System.out.println("\n=== KET QUA: ADMIN APP FLOW TOT! ===");

        } catch (Exception e) {
            System.err.println("LOI!");
            System.err.println("  Type: " + e.getClass().getName());
            System.err.println("  Message: " + e.getMessage());
            System.err.println("\nStack trace:");
            e.printStackTrace();
        }
    }
}
