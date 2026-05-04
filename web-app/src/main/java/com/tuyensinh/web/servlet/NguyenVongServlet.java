package com.tuyensinh.web.servlet;

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

public class NguyenVongServlet extends BaseServlet {

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

            return viewResolver.view("nguyenvong")
                .addObject("thiSinh", thiSinh)
                .addObject("danhSachNguyenVong", xetTuyenService.findNguyenVongByThiSinh(thiSinh.getThisinhId()))
                .addObject("currentPage", "nguyenvong")
                .addObject("pageTitle", "Nguyện vọng");

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

        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            return handleDelete(request, response);
        }
        return handleGet(request, response);
    }

    private ModelAndView handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String nguyenvongIdStr = request.getParameter("nguyenvongId");
        if (nguyenvongIdStr == null || nguyenvongIdStr.trim().isEmpty()) {
            setMessage(request, "Không tìm thấy nguyện vọng cần xóa.", "danger");
            return viewResolver.redirect("/nguyenvong");
        }

        try {
            Integer nguyenvongId = Integer.parseInt(nguyenvongIdStr.trim());
            NguyenVong nguyenVongToDelete = xetTuyenService.findAllNguyenVong().stream()
                    .filter(nv -> nv.getNguyenvongId().equals(nguyenvongId))
                    .findFirst()
                    .orElse(null);

            if (nguyenVongToDelete == null) {
                setMessage(request, "Không tìm thấy nguyện vọng cần xóa.", "danger");
                return viewResolver.redirect("/nguyenvong");
            }

            ThiSinh thiSinh = thiSinhService.findByNguoiDungId(
                authController.getCurrentUser(request).getNguoidungId()).orElse(null);

            if (thiSinh != null
                    && nguyenVongToDelete.getThiSinh().getThisinhId().equals(thiSinh.getThisinhId())) {
                xetTuyenService.deleteNguyenVong(nguyenVongToDelete);
                setMessage(request, "Xóa nguyện vọng thành công!", "success");
            } else {
                setMessage(request, "Bạn không có quyền xóa nguyện vọng này.", "danger");
            }

        } catch (Exception e) {
            setMessage(request, "Đã xảy ra lỗi khi xóa nguyện vọng: " + e.getMessage(), "danger");
        }

        return viewResolver.redirect("/nguyenvong");
    }
}
