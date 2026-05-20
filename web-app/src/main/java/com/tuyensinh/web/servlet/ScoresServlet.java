package com.tuyensinh.web.servlet;

import com.tuyensinh.entity.DiemThi;
import com.tuyensinh.entity.PhuongThuc;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.MonService;
import com.tuyensinh.service.ThiSinhService;
import com.tuyensinh.service.XetTuyenService;
import com.tuyensinh.web.servlet.base.ModelAndView;
import com.tuyensinh.web.servlet.dto.DiemThiForm;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ScoresServlet extends BaseServlet {

    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final XetTuyenService xetTuyenService = new XetTuyenService();
    private final MonService monService = new MonService();

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

            return viewResolver.view("scores")
                .addObject("currentPage", "scores")
                .addObject("thiSinh", thiSinh)
                .addObject("danhSachDiemThi", xetTuyenService.findDiemThiByThiSinhWithDetails(thiSinh.getThisinhId()))
                .addObject("danhSachPhuongThuc", xetTuyenService.findActivePhuongThuc())
                .addObject("danhSachMon", monService.findAll());

        } catch (Exception e) {
            setMessage(request, "Đã xảy ra lỗi: " + e.getMessage(), "danger");
            return viewResolver.redirect("/dashboard");
        }
    }

    @Override
    protected ModelAndView handlePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ModelAndView auth = authController.requireLogin(request, response);
        if (auth != null) return auth;

        try {
            ThiSinh thiSinh = thiSinhService.findByNguoiDungId(
                authController.getCurrentUser(request).getNguoidungId()).orElse(null);

            if (thiSinh == null) {
                setMessage(request, "Không tìm thấy thông tin thí sinh.", "danger");
                return viewResolver.redirect("/scores");
            }

            List<PhuongThuc> danhSachPhuongThuc = xetTuyenService.findActivePhuongThuc();
            List<com.tuyensinh.entity.Mon> danhSachMon = monService.findAll();
            DiemThiForm form = new DiemThiForm(request, danhSachMon);

            PhuongThuc phuongThuc = danhSachPhuongThuc.stream()
                    .filter(pt -> pt.getPhuongthucId().toString().equals(form.getPhuongthucIdStr()))
                    .findFirst()
                    .orElse(null);

            if (phuongThuc == null) {
                setMessage(request, "Không tìm thấy phương thức xét tuyển.", "danger");
                return viewResolver.redirect("/scores");
            }

            Optional<String> validationError = form.validateWithPhuongThuc(phuongThuc);
            if (validationError.isPresent()) {
                setMessage(request, validationError.get(), "danger");
                return viewResolver.redirect("/scores");
            }

            Short namTuyensinh = form.parseNamTuyensinh();
            DiemThi diemThi = form.bindToEntity(phuongThuc, namTuyensinh);
            diemThi.setThiSinh(thiSinh);

            xetTuyenService.saveDiemThi(diemThi);
            setMessage(request, "Lưu điểm thi thành công!", "success");
            return viewResolver.redirect("/scores");

        } catch (Exception e) {
            setMessage(request, "Đã xảy ra lỗi khi lưu điểm thi: " + e.getMessage(), "danger");
            return viewResolver.redirect("/scores");
        }
    }
}
