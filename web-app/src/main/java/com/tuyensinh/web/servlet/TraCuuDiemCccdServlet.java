package com.tuyensinh.web.servlet;

import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import com.tuyensinh.web.servlet.base.ModelAndView;
import com.tuyensinh.web.servlet.base.ViewResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TraCuuDiemCccdServlet extends BaseServlet {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal THIRTY = new BigDecimal("30");
    private static final int SCALE = 3;

    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final XetTuyenService xetTuyenService = new XetTuyenService();
    private final TinhDiemService tinhDiemService = new TinhDiemService();
    private final ViewResolver viewResolver = new ViewResolver();

    // === GET: hiển thị form tra cứu ===
    @Override
    protected ModelAndView handleGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        return viewResolver.view("tra-cuu-diem-cccd")
                .addObject("pageTitle", "Tra cứu kết quả xét tuyển")
                .addObject("currentPage", "tracuu-cccd");
    }

    // === POST: xử lý tra cứu theo CCCD ===
    @Override
    protected ModelAndView handlePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cccd = trim(request.getParameter("cccd"));
        if (cccd == null || cccd.isEmpty()) {
            setFlashMessage(request, "Vui lòng nhập số CCCD/CMND hoặc SBD.", "warning");
            return showForm(request, null, null);
        }

        // Thử tìm theo CCCD trước
        Optional<ThiSinh> opt = thiSinhService.findByCccd(cccd);

        // Nếu không tìm thấy, thử tìm theo SBD
        if (opt.isEmpty()) {
            opt = thiSinhService.findBySoBaoDanh(cccd);
        }

        if (opt.isEmpty()) {
            setFlashMessage(request, "Không tìm thấy thí sinh với CCCD/SBD: " + cccd, "danger");
            return showForm(request, null, null);
        }

        ThiSinh ts = opt.get();
        List<NguyenVong> danhSachNv = xetTuyenService.findNguyenVongByThiSinh(ts.getThisinhId());

        // Chuyển ThiSinh entity → Map để tránh LazyInitializationException trong JSP
        Map<String, Object> tsInfo = buildThiSinhInfo(ts);

        if (danhSachNv == null || danhSachNv.isEmpty()) {
            setFlashMessage(request, "Thí sinh chưa đăng ký nguyện vọng nào.", "info");
            return showFormWithThiSinh(request, tsInfo, List.of());
        }

        List<Map<String, Object>> ketQuaList = buildKetQuaForAll(danhSachNv);
        return showFormWithThiSinh(request, tsInfo, ketQuaList);
    }

    // ================================================================
    // Chuyển ThiSinh entity → Map (tránh lazy loading trong JSP)
    // ================================================================
    private Map<String, Object> buildThiSinhInfo(ThiSinh ts) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("thisinhId", ts.getThisinhId());
        info.put("ho", ts.getHo());
        info.put("ten", ts.getTen());
        info.put("hoVaTen", ts.getHoVaTen());
        info.put("cccd", ts.getCccd());
        info.put("sobaodanh", ts.getSobaodanh());
        info.put("ngaySinh", ts.getNgaySinh());
        info.put("ngaySinhDisplay", ts.getNgaySinhDisplay());
        info.put("gioiTinh", ts.getGioiTinh());
        info.put("dienThoai", ts.getDienThoai());
        info.put("email", ts.getEmail());

        // Đối tượng ưu tiên (eager load nếu chưa load)
        if (ts.getDoiTuongUutien() != null) {
            Map<String, Object> dt = new LinkedHashMap<>();
            dt.put("maDoituong", ts.getDoiTuongUutien().getMaDoituong());
            dt.put("tenDoituong", ts.getDoiTuongUutien().getTenDoituong());
            dt.put("mucDiem", ts.getDoiTuongUutien().getMucDiem());
            info.put("doiTuong", dt);
        }

        // Khu vực ưu tiên (eager load nếu chưa load)
        if (ts.getKhuVucUutien() != null) {
            Map<String, Object> kv = new LinkedHashMap<>();
            kv.put("maKhuvuc", ts.getKhuVucUutien().getMaKhuVuc());
            kv.put("tenKhuvuc", ts.getKhuVucUutien().getTenKhuvuc());
            kv.put("mucDiem", ts.getKhuVucUutien().getMucDiem());
            info.put("khuVuc", kv);
        }

        return info;
    }

    // ================================================================
    // Build kết quả cho tất cả nguyện vọng
    // ================================================================
    private List<Map<String, Object>> buildKetQuaForAll(List<NguyenVong> danhSachNv) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (NguyenVong nv : danhSachNv) {
            Map<String, Object> r = buildKetQuaNguyenVong(nv);
            results.add(r);
        }

        // Sắp xếp theo thứ tự nguyện vọng
        results.sort((a, b) -> {
            int tA = a.get("thuTu") != null ? (int) a.get("thuTu") : 99;
            int tB = b.get("thuTu") != null ? (int) b.get("thuTu") : 99;
            return Integer.compare(tA, tB);
        });

        return results;
    }

    // ================================================================
    // Build kết quả cho 1 nguyện vọng
    // ================================================================
    private Map<String, Object> buildKetQuaNguyenVong(NguyenVong nv) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("thuTu", nv.getThuTu());
        r.put("nguyenVongId", nv.getNguyenvongId());
        r.put("ketQua", nv.getKetQua());

        // Thông tin ngành
        Nganh nganh = nv.getNganh();
        if (nganh != null) {
            Map<String, Object> nganhInfo = new LinkedHashMap<>();
            nganhInfo.put("maNganh", nganh.getMaNganh());
            nganhInfo.put("tenNganh", nganh.getTenNganh());
            nganhInfo.put("diemSan", nganh.getDiemSan());
            nganhInfo.put("diemTrungTuyen", nganh.getDiemTrungTuyen());
            nganhInfo.put("chiTieu", nganh.getChiTieu());
            r.put("nganh", nganhInfo);
        }

        // Thông tin tổ hợp
        NganhToHop nth = nv.getNganhToHop();
        if (nth != null) {
            Map<String, Object> toHopInfo = new LinkedHashMap<>();
            ToHop th = nth.getToHop();
            if (th != null) {
                toHopInfo.put("ma", th.getMaTohop());
                toHopInfo.put("ten", th.getTenTohop());
            }
            toHopInfo.put("doLech", nth.getDoLech());

            // Môn trong tổ hợp
            List<Map<String, Object>> monList = new ArrayList<>();
            if (nth.getDanhSachNganhToHopMon() != null) {
                for (NganhToHopMon nthm : nth.getDanhSachNganhToHopMon()) {
                    if (nthm.getMon() != null) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("maMon", nthm.getMon().getMaMon());
                        m.put("tenMon", nthm.getMon().getTenMon());
                        m.put("heSo", nthm.getHeSo() != null ? nthm.getHeSo().intValue() : 1);
                        monList.add(m);
                    }
                }
            }
            toHopInfo.put("monList", monList);
            r.put("toHop", toHopInfo);
        }

        // Tính điểm chi tiết bằng TinhDiemService
        TinhDiemService.KetQuaDiem kqDiem = null;
        try {
            kqDiem = tinhDiemService.tinhDiemTotNhat(nv);
        } catch (Exception e) {
            // Bỏ qua lỗi tính điểm, hiển thị thông tin cơ bản
        }

        if (kqDiem != null) {
            Map<String, Object> diemInfo = new LinkedHashMap<>();
            diemInfo.put("diemThxt", kqDiem.diemThxt);
            diemInfo.put("diemCong", kqDiem.diemCong);
            diemInfo.put("diemUutien", kqDiem.diemUutien);
            diemInfo.put("diemXettuyen", kqDiem.diemXettuyen);
            diemInfo.put("phuongThucDiemTotNhat", kqDiem.phuongThucDiemTotNhat);
            diemInfo.put("ghiChu", kqDiem.ghiChu);
            diemInfo.put("coBangQuyDoi", kqDiem.coBangQuyDoi);
            diemInfo.put("diemThpt", kqDiem.diemThpt);
            diemInfo.put("diemVsat", kqDiem.diemVsat);
            diemInfo.put("diemDgnl", kqDiem.diemDgnl);
            r.put("diemInfo", diemInfo);
        }

        // Điểm thi theo từng phương thức
        r.put("danhSachDiemTheoPhuongThuc", buildDiemTheoPhuongThuc(nv));

        return r;
    }

    // ================================================================
    // Build điểm theo từng phương thức cho 1 nguyện vọng
    // ================================================================
    private List<Map<String, Object>> buildDiemTheoPhuongThuc(NguyenVong nv) {
        List<Map<String, Object>> results = new ArrayList<>();

        // Lấy tất cả phương thức active
        List<PhuongThuc> phuongThucs = xetTuyenService.findActivePhuongThuc();
        if (phuongThucs == null) return results;

        for (PhuongThuc pt : phuongThucs) {
            if (pt == null || pt.getPhuongthucId() == null) continue;

            // Tạo nguyện vọng tạm để tính điểm theo phương thức này
            NguyenVong nvTam = new NguyenVong();
            nvTam.setNguyenvongId(nv.getNguyenvongId());
            nvTam.setThiSinh(nv.getThiSinh());
            nvTam.setNganh(nv.getNganh());
            nvTam.setNganhToHop(nv.getNganhToHop());
            nvTam.setPhuongThuc(pt);
            nvTam.setThuTu(nv.getThuTu());

            TinhDiemService.KetQuaDiem kq = null;
            try {
                kq = tinhDiemService.tinhDiemTotNhat(nvTam);
            } catch (Exception ignored) {
            }

            if (kq != null && kq.diemXettuyen != null && kq.diemXettuyen.compareTo(ZERO) > 0) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("phuongThucId", pt.getPhuongthucId());
                item.put("maPhuongThuc", pt.getMaPhuongthuc());
                item.put("tenPhuongThuc", pt.getTenPhuongthuc());
                item.put("diemThxt", kq.diemThxt);
                item.put("diemCong", kq.diemCong);
                item.put("diemUutien", kq.diemUutien);
                item.put("diemXettuyen", kq.diemXettuyen);
                item.put("phuongThucDiemTotNhat", kq.phuongThucDiemTotNhat);
                item.put("ghiChu", kq.ghiChu);
                item.put("coBangQuyDoi", kq.coBangQuyDoi);
                results.add(item);
            }
        }

        // Sắp xếp theo điểm xét tuyển giảm dần
        results.sort((a, b) -> {
            BigDecimal da = a.get("diemXettuyen") != null ? (BigDecimal) a.get("diemXettuyen") : ZERO;
            BigDecimal db = b.get("diemXettuyen") != null ? (BigDecimal) b.get("diemXettuyen") : ZERO;
            return db.compareTo(da);
        });

        return results;
    }

    // ================================================================
    // Helpers
    // ================================================================
    private ModelAndView showForm(HttpServletRequest request, Map<String, Object> thiSinhInfo, List<Map<String, Object>> results) {
        ModelAndView mav = viewResolver.view("tra-cuu-diem-cccd")
                .addObject("pageTitle", "Tra cứu kết quả xét tuyển")
                .addObject("currentPage", "tracuu-cccd");
        if (thiSinhInfo != null) mav.addObject("thiSinh", thiSinhInfo);
        if (results != null) mav.addObject("ketQuaList", results);
        return mav;
    }

    private ModelAndView showFormWithThiSinh(HttpServletRequest request, Map<String, Object> thiSinhInfo, List<Map<String, Object>> results) {
        return showForm(request, thiSinhInfo, results);
    }

    private void setFlashMessage(HttpServletRequest request, String msg, String type) {
        request.getSession().setAttribute("message", msg);
        request.getSession().setAttribute("messageType", type);
    }

    private String trim(String s) {
        return (s != null) ? s.trim() : null;
    }

    public static String formatDiem(BigDecimal d) {
        if (d == null) return "–";
        return d.setScale(3, RoundingMode.HALF_UP).toPlainString();
    }

    public static String formatDate(LocalDate d) {
        if (d == null) return "–";
        return d.format(DF);
    }

    public static String tenKetQua(String kq) {
        if (kq == null) return "Chưa xét";
        switch (kq) {
            case "TRUNG_TUYEN": return "Trúng tuyển";
            case "TRUOT": return "Không trúng tuyển";
            case "CHO_XET": return "Chờ xét";
            case "PHOI_DU_KIEN": return "Phôi dự kiến";
            default: return kq;
        }
    }

    public static String cssKetQua(String kq) {
        if (kq == null) return "secondary";
        switch (kq) {
            case "TRUNG_TUYEN": return "success";
            case "TRUOT": return "danger";
            case "CHO_XET": return "warning";
            case "PHOI_DU_KIEN": return "info";
            default: return "secondary";
        }
    }
}
