package com.tuyensinh.web.servlet;

import com.tuyensinh.entity.NguoiDung;
import com.tuyensinh.entity.NguyenVong;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.ThiSinhService;
import com.tuyensinh.service.XetTuyenService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class NguyenVongServlet extends BaseServlet {

    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final XetTuyenService xetTuyenService = new XetTuyenService();

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
                setMessage(request, "Khong tim thay thong tin thi sinh.", "warning");
                redirect(response, request.getContextPath() + "/profile");
                return;
            }

            List<NguyenVong> danhSachNguyenVong = xetTuyenService.findNguyenVongByThiSinh(thiSinh.getThisinhId());

            setAttribute(request, "thiSinh", thiSinh);
            setAttribute(request, "danhSachNguyenVong", danhSachNguyenVong);

            forward(request, response, getViewPath("nguyenvong"));

        } catch (Exception e) {
            setMessage(request, "Da xay ra loi: " + e.getMessage(), "danger");
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

        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            handleDelete(request, response);
        } else {
            handleGet(request, response);
        }
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String nguyenvongIdStr = request.getParameter("nguyenvongId");

            if (isNullOrEmpty(nguyenvongIdStr)) {
                setMessage(request, "Khong tim thay nguyen vong can xoa.", "danger");
                redirect(response, request.getContextPath() + "/nguyenvong");
                return;
            }

            Integer nguyenvongId = Integer.parseInt(nguyenvongIdStr);

            List<NguyenVong> allNguyenVong = xetTuyenService.findAllNguyenVong();
            NguyenVong nguyenVongToDelete = null;

            for (NguyenVong nv : allNguyenVong) {
                if (nv.getNguyenvongId().equals(nguyenvongId)) {
                    nguyenVongToDelete = nv;
                    break;
                }
            }

            if (nguyenVongToDelete == null) {
                setMessage(request, "Khong tim thay nguyen vong can xoa.", "danger");
                redirect(response, request.getContextPath() + "/nguyenvong");
                return;
            }

            NguoiDung loggedInUser = getLoggedInUser(request);
            ThiSinh thiSinh = thiSinhService.findByNguoiDungId(loggedInUser.getNguoidungId()).orElse(null);

            if (thiSinh != null
                    && nguyenVongToDelete.getThiSinh().getThisinhId().equals(thiSinh.getThisinhId())) {
                xetTuyenService.deleteNguyenVong(nguyenVongToDelete);
                setMessage(request, "Xoa nguyen vong thanh cong!", "success");
            } else {
                setMessage(request, "Ban khong co quyen xoa nguyen vong nay.", "danger");
            }

        } catch (Exception e) {
            setMessage(request, "Da xay ra loi khi xoa nguyen vong: " + e.getMessage(), "danger");
        }

        redirect(response, request.getContextPath() + "/nguyenvong");
    }
}
