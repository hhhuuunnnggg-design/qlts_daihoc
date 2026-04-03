package com.tuyensinh.web.servlet;

import com.tuyensinh.entity.DiemThi;
import com.tuyensinh.entity.DiemThiChiTiet;
import com.tuyensinh.entity.Mon;
import com.tuyensinh.entity.NguoiDung;
import com.tuyensinh.entity.PhuongThuc;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.MonService;
import com.tuyensinh.service.ThiSinhService;
import com.tuyensinh.service.XetTuyenService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ScoresServlet extends BaseServlet {

    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final XetTuyenService xetTuyenService = new XetTuyenService();
    private final MonService monService = new MonService();

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        NguoiDung loggedInUser = getLoggedInUser(request);
        if (loggedInUser == null) {
            requireLogin(request, response);
            return;
        }

        try {
            ThiSinh thiSinh = thiSinhService.findByNguoiDungId(loggedInUser.getNguoidungId()).orElse(null);

            if (thiSinh == null) {
                setMessage(request, "Không tìm thấy thông tin thí sinh.", "warning");
                redirect(response, request.getContextPath() + "/profile");
                return;
            }

            List<DiemThi> danhSachDiemThi = xetTuyenService.findDiemThiByThiSinh(thiSinh.getThisinhId());
            List<PhuongThuc> danhSachPhuongThuc = xetTuyenService.findActivePhuongThuc();
            List<Mon> danhSachMon = monService.findAll();

            setAttribute(request, "thiSinh", thiSinh);
            setAttribute(request, "danhSachDiemThi", danhSachDiemThi);
            setAttribute(request, "danhSachPhuongThuc", danhSachPhuongThuc);
            setAttribute(request, "danhSachMon", danhSachMon);

            forward(request, response, getViewPath("scores"));

        } catch (Exception e) {
            setMessage(request, "Đã xảy ra lỗi: " + e.getMessage(), "danger");
            redirect(response, request.getContextPath() + "/dashboard");
        }
    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        NguoiDung loggedInUser = getLoggedInUser(request);
        if (loggedInUser == null) {
            requireLogin(request, response);
            return;
        }

        try {
            ThiSinh thiSinh = thiSinhService.findByNguoiDungId(loggedInUser.getNguoidungId()).orElse(null);

            if (thiSinh == null) {
                setMessage(request, "Không tìm thấy thông tin thí sinh.", "danger");
                redirect(response, request.getContextPath() + "/scores");
                return;
            }

            String phuongthucIdStr = request.getParameter("phuongthucId");
            String namTuyensinhStr = request.getParameter("namTuyensinh");
            String sobaodanh = request.getParameter("sobaodanh");
            String ghiChu = request.getParameter("ghiChu");

            if (isNullOrEmpty(phuongthucIdStr)) {
                setMessage(request, "Vui lòng chọn phương thức xét tuyển.", "danger");
                redirect(response, request.getContextPath() + "/scores");
                return;
            }

            PhuongThuc phuongThuc = xetTuyenService.findPhuongThucByMa(
                xetTuyenService.findActivePhuongThuc()
                    .stream()
                    .filter(pt -> pt.getPhuongthucId().toString().equals(phuongthucIdStr))
                    .findFirst()
                    .map(PhuongThuc::getMaPhuongthuc)
                    .orElse(null)
            ).orElse(null);

            if (phuongThuc == null) {
                List<PhuongThuc> phuongThucs = xetTuyenService.findActivePhuongThuc();
                for (PhuongThuc pt : phuongThucs) {
                    if (pt.getPhuongthucId().toString().equals(phuongthucIdStr)) {
                        phuongThuc = pt;
                        break;
                    }
                }
            }

            Short namTuyensinh = 2026;
            if (!isNullOrEmpty(namTuyensinhStr)) {
                try {
                    namTuyensinh = Short.parseShort(namTuyensinhStr);
                } catch (NumberFormatException e) {
                }
            }

            DiemThi diemThi = new DiemThi();
            diemThi.setThiSinh(thiSinh);
            diemThi.setPhuongThuc(phuongThuc);
            diemThi.setNamTuyensinh(namTuyensinh);
            diemThi.setSobaodanh(sobaodanh);
            diemThi.setGhiChu(ghiChu);

            DiemThi savedDiemThi = xetTuyenService.saveDiemThi(diemThi);
            if (savedDiemThi.getDanhSachDiemChiTiet() == null) {
                savedDiemThi.setDanhSachDiemChiTiet(new ArrayList<>());
            }

            List<Mon> danhSachMon = monService.findAll();
            for (Mon mon : danhSachMon) {
                String diemStr = request.getParameter("diem_" + mon.getMonId());
                if (diemStr != null && !diemStr.trim().isEmpty()) {
                    try {
                        BigDecimal diemGoc = new BigDecimal(diemStr.trim());
                        DiemThiChiTiet chiTiet = new DiemThiChiTiet();
                        chiTiet.setDiemThi(savedDiemThi);
                        chiTiet.setMon(mon);
                        chiTiet.setDiemGoc(diemGoc);
                        chiTiet.setDiemQuydoi(diemGoc);
                        chiTiet.setDiemSudung(diemGoc);

                        savedDiemThi.getDanhSachDiemChiTiet().add(chiTiet);
                    } catch (NumberFormatException e) {
                    }
                }
            }

            xetTuyenService.updateDiemThi(savedDiemThi);

            setMessage(request, "Lưu điểm thi thành công!", "success");
            redirect(response, request.getContextPath() + "/scores");

        } catch (Exception e) {
            setMessage(request, "Đã xảy ra lỗi khi lưu điểm thi: " + e.getMessage(), "danger");
            redirect(response, request.getContextPath() + "/scores");
        }
    }
}
