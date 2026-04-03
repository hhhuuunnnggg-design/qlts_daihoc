package com.tuyensinh.util;

import com.tuyensinh.entity.DoiTuongUutien;
import com.tuyensinh.entity.KhuVucUutien;
import com.tuyensinh.entity.Mon;
import com.tuyensinh.entity.Nganh;
import com.tuyensinh.entity.NganhPhuongThuc;
import com.tuyensinh.entity.NganhToHop;
import com.tuyensinh.entity.NguoiDung;
import com.tuyensinh.entity.PhuongThuc;
import com.tuyensinh.entity.ToHop;
import com.tuyensinh.entity.ToHopMon;
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

        // --- Phương thức tuyển sinh ---
        getOrCreatePhuongThuc(session, PhuongThuc.XTT,
                "Xét tuyển thẳng",
                new BigDecimal("30.00"),
                true);

        getOrCreatePhuongThuc(session, PhuongThuc.THPT,
                "Xét tuyển theo điểm thi THPT",
                new BigDecimal("30.00"),
                true);

        getOrCreatePhuongThuc(session, PhuongThuc.VSAT,
                "Xét tuyển theo điểm thi V-SAT",
                new BigDecimal("30.00"),
                true);

        getOrCreatePhuongThuc(session, PhuongThuc.DGNL,
                "Xét tuyển theo kết quả bài thi đánh giá năng lực",
                new BigDecimal("30.00"),
                true);

        getOrCreatePhuongThuc(session, PhuongThuc.NK,
                "Xét tuyển theo điểm thi năng khiếu",
                new BigDecimal("30.00"),
                true);

        // --- Môn học lớp 12 (phục vụ xét tuyển THPT & NK) ---
        // --- Môn học ---
        getOrCreateMon(session, "TO",   "Toán", Mon.LoaiMon.MON_HOC);
        getOrCreateMon(session, "VA",   "Ngữ văn", Mon.LoaiMon.MON_HOC);
        getOrCreateMon(session, "LI",   "Vật lý", Mon.LoaiMon.MON_HOC);
        getOrCreateMon(session, "HO",   "Hóa học", Mon.LoaiMon.MON_HOC);
        getOrCreateMon(session, "SI",   "Sinh học", Mon.LoaiMon.MON_HOC);
        getOrCreateMon(session, "SU",   "Lịch sử", Mon.LoaiMon.MON_HOC);
        getOrCreateMon(session, "DI",   "Địa lý", Mon.LoaiMon.MON_HOC);
        getOrCreateMon(session, "N1",   "Ngoại ngữ", Mon.LoaiMon.MON_HOC);

        getOrCreateMon(session, "TI",   "Tin học", Mon.LoaiMon.MON_HOC);
        getOrCreateMon(session, "KTPL", "Giáo dục kinh tế và pháp luật", Mon.LoaiMon.MON_HOC);
        getOrCreateMon(session, "CNCN", "Công nghệ công nghiệp", Mon.LoaiMon.MON_HOC);
        getOrCreateMon(session, "CNNN", "Công nghệ nông nghiệp", Mon.LoaiMon.MON_HOC);

        getOrCreateMon(session, "NL1", "Đánh giá năng lực", Mon.LoaiMon.DANH_GIA_NANG_LUC);

        getOrCreateMon(session, "NK1", "Năng khiếu 1", Mon.LoaiMon.NANG_KHIEU);
        getOrCreateMon(session, "NK2", "Năng khiếu 2", Mon.LoaiMon.NANG_KHIEU);
        getOrCreateMon(session, "NK3", "Năng khiếu 3", Mon.LoaiMon.NANG_KHIEU);
        getOrCreateMon(session, "NK4", "Năng khiếu 4", Mon.LoaiMon.NANG_KHIEU);
        getOrCreateMon(session, "NK5", "Năng khiếu 5", Mon.LoaiMon.NANG_KHIEU);
        getOrCreateMon(session, "NK6", "Năng khiếu 6", Mon.LoaiMon.NANG_KHIEU);

        // --- Tổ hợp môn xét tuyển ---
        ToHop thA00 = getOrCreateToHop(session, "A00", "Toán - Vật lý - Hóa học");
        ToHop thA01 = getOrCreateToHop(session, "A01", "Toán - Vật lý - Ngoại ngữ");
        ToHop thA02 = getOrCreateToHop(session, "A02", "Toán - Vật lý - Sinh học");
        ToHop thA05 = getOrCreateToHop(session, "A05", "Toán - Ngữ văn - Lịch sử");
        ToHop thA07 = getOrCreateToHop(session, "A07", "Toán - Địa lý - KTPL");
        ToHop thA14 = getOrCreateToHop(session, "A14", "Toán - Ngữ văn - KTPL");
        ToHop thB00 = getOrCreateToHop(session, "B00", "Toán - Hóa học - Ngữ văn");
        ToHop thC00 = getOrCreateToHop(session, "C00", "Ngữ văn - Lịch sử - Địa lý");
        ToHop thC14 = getOrCreateToHop(session, "C14", "Ngữ văn - KTPL - Lịch sử");
        ToHop thD01 = getOrCreateToHop(session, "D01", "Toán - Ngữ văn - Ngoại ngữ");
        ToHop thD07 = getOrCreateToHop(session, "D07", "Toán - Lịch sử - KTPL");
        ToHop thD08 = getOrCreateToHop(session, "D08", "Toán - Địa lý - Ngoại ngữ");

        ensureToHopMon(session, thA00, "TO", "LI", "HO");
        ensureToHopMon(session, thA01, "TO", "LI", "N1");
        ensureToHopMon(session, thA02, "TO", "LI", "SI");
        ensureToHopMon(session, thA05, "TO", "VA", "SU");
        ensureToHopMon(session, thA07, "TO", "DI", "KTPL");
        ensureToHopMon(session, thA14, "TO", "VA", "KTPL");
        ensureToHopMon(session, thB00, "TO", "HO", "VA");
        ensureToHopMon(session, thC00, "VA", "SU", "DI");
        ensureToHopMon(session, thC14, "VA", "KTPL", "SU");
        ensureToHopMon(session, thD01, "TO", "VA", "N1");
        ensureToHopMon(session, thD07, "TO", "SU", "KTPL");
        ensureToHopMon(session, thD08, "TO", "DI", "N1");

        // --- Ngành / CTĐT mẫu (chỉ tiêu, điểm sàn, điểm trúng tuyển giả định) ---
        PhuongThuc ptThpt = findPhuongThucByMa(session, PhuongThuc.THPT);
        if (ptThpt != null) {
            Nganh nganhCntt = getOrCreateNganh(session, "7480201",
                    "Công nghệ thông tin", 400,
                    new BigDecimal("18.00"), new BigDecimal("25.75"), thA00);
            Nganh nganhQtkd = getOrCreateNganh(session, "7340101",
                    "Quản trị kinh doanh", 360,
                    new BigDecimal("17.50"), new BigDecimal("23.20"), thD01);
            Nganh nganhKeToan = getOrCreateNganh(session, "7340301",
                    "Kế toán", 380,
                    new BigDecimal("17.00"), new BigDecimal("22.80"), thD01);
            Nganh nganhTcNh = getOrCreateNganh(session, "7340201",
                    "Tài chính - Ngân hàng", 500,
                    new BigDecimal("17.50"), new BigDecimal("23.50"), thD01);
            Nganh nganhNna = getOrCreateNganh(session, "7220201",
                    "Ngôn ngữ Anh", 253,
                    new BigDecimal("18.00"), new BigDecimal("26.10"), thD01);
            Nganh nganhKtpm = getOrCreateNganh(session, "7480103",
                    "Kỹ thuật phần mềm", 110,
                    new BigDecimal("18.50"), new BigDecimal("26.40"), thA00);
            Nganh nganhGdmn = getOrCreateNganh(session, "7140201",
                    "Giáo dục Mầm non", 200,
                    new BigDecimal("16.00"), new BigDecimal("21.00"), thC00);
            Nganh nganhSptoan = getOrCreateNganh(session, "7140209",
                    "Sư phạm Toán học", 40,
                    new BigDecimal("17.00"), new BigDecimal("24.50"), thA00);

            ensureNganhToHop(session, nganhCntt, thA00, BigDecimal.ZERO);
            ensureNganhToHop(session, nganhCntt, thA01, BigDecimal.ZERO);
            ensureNganhToHop(session, nganhCntt, thD01, BigDecimal.ZERO);
            ensureNganhToHop(session, nganhQtkd, thD01, BigDecimal.ZERO);
            ensureNganhToHop(session, nganhQtkd, thA14, BigDecimal.ZERO);
            ensureNganhToHop(session, nganhKeToan, thD01, BigDecimal.ZERO);
            ensureNganhToHop(session, nganhTcNh, thD01, BigDecimal.ZERO);
            ensureNganhToHop(session, nganhNna, thD01, BigDecimal.ZERO);
            ensureNganhToHop(session, nganhKtpm, thA00, BigDecimal.ZERO);
            ensureNganhToHop(session, nganhKtpm, thA01, BigDecimal.ZERO);
            ensureNganhToHop(session, nganhGdmn, thC00, BigDecimal.ZERO);
            ensureNganhToHop(session, nganhGdmn, thC14, BigDecimal.ZERO);
            ensureNganhToHop(session, nganhSptoan, thA00, BigDecimal.ZERO);
            ensureNganhToHop(session, nganhSptoan, thA01, BigDecimal.ZERO);

            ensureNganhPhuongThuc(session, nganhCntt, ptThpt, nganhCntt.getChiTieu());
            ensureNganhPhuongThuc(session, nganhQtkd, ptThpt, nganhQtkd.getChiTieu());
            ensureNganhPhuongThuc(session, nganhKeToan, ptThpt, nganhKeToan.getChiTieu());
            ensureNganhPhuongThuc(session, nganhTcNh, ptThpt, nganhTcNh.getChiTieu());
            ensureNganhPhuongThuc(session, nganhNna, ptThpt, nganhNna.getChiTieu());
            ensureNganhPhuongThuc(session, nganhKtpm, ptThpt, nganhKtpm.getChiTieu());
            ensureNganhPhuongThuc(session, nganhGdmn, ptThpt, nganhGdmn.getChiTieu());
            ensureNganhPhuongThuc(session, nganhSptoan, ptThpt, nganhSptoan.getChiTieu());
        }

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

    private static PhuongThuc getOrCreatePhuongThuc(Session session, String maPhuongthuc,
            String tenPhuongthuc, BigDecimal thangDiem, boolean isActive) {
        @SuppressWarnings("unchecked")
        PhuongThuc existing = (PhuongThuc) session
            .createQuery("FROM PhuongThuc pt WHERE pt.maPhuongthuc = :ma")
            .setParameter("ma", maPhuongthuc)
            .setMaxResults(1)
            .uniqueResult();
        if (existing != null) return existing;

        PhuongThuc pt = new PhuongThuc();
        pt.setMaPhuongthuc(maPhuongthuc);
        pt.setTenPhuongthuc(tenPhuongthuc);
        pt.setThangDiem(thangDiem);
        pt.setIsActive(isActive);
        session.persist(pt);
        return pt;
    }

    private static Mon getOrCreateMon(Session session, String maMon, String tenMon, String loaiMon) {
        @SuppressWarnings("unchecked")
        Mon existing = (Mon) session
            .createQuery("FROM Mon m WHERE m.maMon = :ma")
            .setParameter("ma", maMon)
            .setMaxResults(1)
            .uniqueResult();
        if (existing != null) return existing;

        Mon m = new Mon();
        m.setMaMon(maMon);
        m.setTenMon(tenMon);
        m.setLoaiMon(loaiMon);
        session.persist(m);
        return m;
    }

    private static ToHop getOrCreateToHop(Session session, String maTohop, String tenTohop) {
        @SuppressWarnings("unchecked")
        ToHop existing = (ToHop) session
            .createQuery("FROM ToHop t WHERE t.maTohop = :ma")
            .setParameter("ma", maTohop)
            .setMaxResults(1)
            .uniqueResult();
        if (existing != null) return existing;

        ToHop t = new ToHop();
        t.setMaTohop(maTohop);
        t.setTenTohop(tenTohop);
        session.persist(t);
        session.flush();
        return t;
    }

    private static Mon findMonByMa(Session session, String maMon) {
        @SuppressWarnings("unchecked")
        Mon mon = (Mon) session
            .createQuery("FROM Mon m WHERE m.maMon = :ma")
            .setParameter("ma", maMon)
            .setMaxResults(1)
            .uniqueResult();
        return mon;
    }

    private static void ensureToHopMon(Session session, ToHop toHop, String... monMaOrdered) {
        Long count = (Long) session
            .createQuery("SELECT COUNT(thm.tohopMonId) FROM ToHopMon thm WHERE thm.toHop.tohopId = :tid")
            .setParameter("tid", toHop.getTohopId())
            .uniqueResult();
        if (count != null && count > 0) return;

        short order = 1;
        for (String ma : monMaOrdered) {
            Mon mon = findMonByMa(session, ma);
            if (mon == null) {
                throw new IllegalStateException("[DataSeeder] Mon khong ton tai: " + ma);
            }
            ToHopMon thm = new ToHopMon();
            thm.setToHop(toHop);
            thm.setMon(mon);
            thm.setThuTu(order++);
            session.persist(thm);
        }
    }

    private static PhuongThuc findPhuongThucByMa(Session session, String ma) {
        @SuppressWarnings("unchecked")
        PhuongThuc pt = (PhuongThuc) session
            .createQuery("FROM PhuongThuc p WHERE p.maPhuongthuc = :ma")
            .setParameter("ma", ma)
            .setMaxResults(1)
            .uniqueResult();
        return pt;
    }

    private static Nganh getOrCreateNganh(Session session, String maNganh, String tenNganh,
            Integer chiTieu, BigDecimal diemSan, BigDecimal diemTrungTuyen, ToHop toHopGoc) {
        @SuppressWarnings("unchecked")
        Nganh existing = (Nganh) session
            .createQuery("FROM Nganh n WHERE n.maNganh = :ma")
            .setParameter("ma", maNganh)
            .setMaxResults(1)
            .uniqueResult();
        if (existing != null) return existing;

        Nganh n = new Nganh();
        n.setMaNganh(maNganh);
        n.setTenNganh(tenNganh);
        n.setChiTieu(chiTieu);
        n.setDiemSan(diemSan);
        n.setDiemTrungTuyen(diemTrungTuyen);
        n.setToHopGoc(toHopGoc);
        n.setIsActive(true);
        session.persist(n);
        session.flush();
        return n;
    }

    private static void ensureNganhToHop(Session session, Nganh nganh, ToHop toHop, BigDecimal doLech) {
        @SuppressWarnings("unchecked")
        NganhToHop existing = (NganhToHop) session
            .createQuery("FROM NganhToHop nth WHERE nth.nganh.nganhId = :nid AND nth.toHop.tohopId = :tid")
            .setParameter("nid", nganh.getNganhId())
            .setParameter("tid", toHop.getTohopId())
            .setMaxResults(1)
            .uniqueResult();
        if (existing != null) return;

        NganhToHop nth = new NganhToHop();
        nth.setNganh(nganh);
        nth.setToHop(toHop);
        nth.setDoLech(doLech);
        session.persist(nth);
    }

    private static void ensureNganhPhuongThuc(Session session, Nganh nganh, PhuongThuc pt, Integer chiTieu) {
        @SuppressWarnings("unchecked")
        NganhPhuongThuc existing = (NganhPhuongThuc) session
            .createQuery("FROM NganhPhuongThuc np WHERE np.nganh.nganhId = :nid "
                    + "AND np.phuongThuc.phuongthucId = :pid")
            .setParameter("nid", nganh.getNganhId())
            .setParameter("pid", pt.getPhuongthucId())
            .setMaxResults(1)
            .uniqueResult();
        if (existing != null) return;

        NganhPhuongThuc np = new NganhPhuongThuc();
        np.setNganh(nganh);
        np.setPhuongThuc(pt);
        np.setChiTieu(chiTieu);
        np.setSoLuongHienTai(0);
        np.setIsEnabled(true);
        session.persist(np);
    }
}
