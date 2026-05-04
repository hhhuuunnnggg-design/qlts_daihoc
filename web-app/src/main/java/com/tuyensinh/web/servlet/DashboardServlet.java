package com.tuyensinh.web.servlet;

import com.tuyensinh.entity.DoiTuongUutien;
import com.tuyensinh.entity.KhuVucUutien;
import com.tuyensinh.entity.NguyenVong;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.ThiSinhService;
import com.tuyensinh.service.XetTuyenService;
import com.tuyensinh.web.servlet.base.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class DashboardServlet extends BaseServlet {

    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final XetTuyenService xetTuyenService = new XetTuyenService();

    @Override
    protected ModelAndView handleGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ModelAndView auth = authController.requireLogin(request, response);
        if (auth != null) return auth;

        try {
            ThiSinh thiSinh = thiSinhService.findByNguoiDungId(
                authController.getCurrentUser(request).getNguoidungId()).orElse(null);

            if (thiSinh == null) {
                setMessage(request, "Không tìm thấy thông tin thí sinh.", "warning");
                return viewResolver.redirect("/profile");
            }

            List<NguyenVong> danhSachNguyenVong = xetTuyenService.findNguyenVongByThiSinh(thiSinh.getThisinhId());
            List<?> danhSachDiemThi = xetTuyenService.findDiemThiByThiSinh(thiSinh.getThisinhId());

            int soTrungTuyen = 0;
            for (NguyenVong nv : danhSachNguyenVong) {
                if (NguyenVong.KetQua.TRUNG_TUYEN.equals(nv.getKetQua())) {
                    soTrungTuyen++;
                }
            }

            return viewResolver.view("dashboard")
                .addObject("thiSinh", thiSinh)
                .addObject("danhSachNguyenVong", danhSachNguyenVong)
                .addObject("soLuongNguyenVong", danhSachNguyenVong.size())
                .addObject("soLuongDiemThi", danhSachDiemThi.size())
                .addObject("soTrungTuyen", soTrungTuyen);

        } catch (Exception e) {
            setMessage(request, "Đã xảy ra lỗi: " + e.getMessage(), "danger");
            return viewResolver.view("dashboard");
        }
    }

    @Override
    protected ModelAndView handlePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        return handleGet(request, response);
    }
}
