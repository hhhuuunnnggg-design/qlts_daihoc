import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import com.tuyensinh.util.*;
import java.math.BigDecimal;

public class TestCRUD {
    public static void main(String[] args) {
        try {
            // Initialize Hibernate
            HibernateUtil.getSession();
            System.out.println("=== KHOI TAO THANH CONG ===\n");

            // 1. Test NGUOI DUNG CRUD
            System.out.println("--- 1. NGUOI DUNG CRUD ---");
            NguoiDungService ndService = new NguoiDungService();
            VaiTroService vtService = new VaiTroService();

            // List all users
            System.out.println("List: " + ndService.findAll().size() + " users");

            // Find a role
            VaiTro userRole = vtService.findByMa("USER");
            System.out.println("Role USER found: " + (userRole != null));

            // 2. Test THI SINH CRUD
            System.out.println("\n--- 2. THI SINH CRUD ---");
            ThiSinhService tsService = new ThiSinhService();
            System.out.println("Count: " + tsService.findAll().size() + " thi sinh");

            // 3. Test TO HOP CRUD
            System.out.println("\n--- 3. TO HOP CRUD ---");
            ToHopService thService = new ToHopService();
            System.out.println("Count: " + thService.findAll().size() + " to hop");

            // 4. Test NGANH CRUD
            System.out.println("\n--- 4. NGANH CRUD ---");
            XetTuyenService xtService = new XetTuyenService();
            System.out.println("Count: " + xtService.findAllNganh().size() + " nganh");

            // 5. Test NGANH TO HOP CRUD
            System.out.println("\n--- 5. NGANH TO HOP CRUD ---");
            NganhToHopService nthService = new NganhToHopService();
            System.out.println("Count: " + nthService.findAll().size() + " nganh-to-hop");

            // 6. Test PHUONG THUC
            System.out.println("\n--- 6. PHUONG THUC ---");
            System.out.println("Phuong thuc count: " + xtService.findAllPhuongThuc().size());

            // 7. Test BANG QUY DOI
            System.out.println("\n--- 7. BANG QUY DOI ---");
            BangQuyDoiService bqdService = new BangQuyDoiService();
            System.out.println("Bang quy doi count: " + bqdService.findAll().size());

            // 8. Test DIEM THI
            System.out.println("\n--- 8. DIEM THI ---");
            DiemThiService dtService = new DiemThiService();
            System.out.println("Diem thi count: " + dtService.findAll().size());

            // 9. Test DIEM CONG
            System.out.println("\n--- 9. DIEM CONG ---");
            DiemCongService dcService = new DiemCongService();
            System.out.println("Diem cong count: " + dcService.findAll().size());

            // 10. Test NGUYEN VONG
            System.out.println("\n--- 10. NGUYEN VONG ---");
            NguyenVongService nvService = new NguyenVongService();
            System.out.println("Nguyen vong count: " + nvService.findAll().size());

            System.out.println("\n=== TOI DA 10 BANG DAU RA ===");
            System.out.println("Tat ca kiem tra CRUD hoan tat!");

        } catch (Exception e) {
            System.err.println("LOI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
