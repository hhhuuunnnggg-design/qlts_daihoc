package com.tuyensinh.web.servlet;

import com.tuyensinh.entity.DoiTuongUutien;
import com.tuyensinh.entity.KhuVucUutien;
import com.tuyensinh.entity.NguoiDung;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.service.DoiTuongService;
import com.tuyensinh.service.KhuVucService;
import com.tuyensinh.service.ThiSinhService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ProfileServlet extends BaseServlet {

    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final DoiTuongService doiTuongService = new DoiTuongService();
    private final KhuVucService khuVucService = new KhuVucService();

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        NguoiDung loggedInUser = getLoggedInUser(request);
        if (loggedInUser == null) {
            requireLogin(request, response);
            return;
        }

        try {
            ThiSinh thiSinh = thiSinhService.findById(loggedInUser.getNguoidungId());
            List<DoiTuongUutien> danhSachDoiTuong = doiTuongService.findAll();
            List<KhuVucUutien> danhSachKhuVuc = khuVucService.findAll();

            setAttribute(request, "thiSinh", thiSinh);
            setAttribute(request, "danhSachDoiTuong", danhSachDoiTuong);
            setAttribute(request, "danhSachKhuVuc", danhSachKhuVuc);

            forward(request, response, getViewPath("profile"));

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

        try {
            ThiSinh thiSinh = thiSinhService.findById(loggedInUser.getNguoidungId());

            if (thiSinh == null) {
                setMessage(request, "Khong tim thay thong tin thi sinh.", "danger");
                redirect(response, request.getContextPath() + "/profile");
                return;
            }

            String ho = request.getParameter("ho");
            String ten = request.getParameter("ten");
            String ngaySinhStr = request.getParameter("ngaySinh");
            String gioiTinh = request.getParameter("gioiTinh");
            String dienThoai = request.getParameter("dienThoai");
            String email = request.getParameter("email");
            String noiSinh = request.getParameter("noiSinh");
            String doituongIdStr = request.getParameter("doituongId");
            String khuvucIdStr = request.getParameter("khuvucId");

            if (isNullOrEmpty(ho) || isNullOrEmpty(ten)) {
                setMessage(request, "Ho va ten khong duoc de trong.", "danger");
                redirect(response, request.getContextPath() + "/profile");
                return;
            }

            thiSinh.setHo(ho.trim());
            thiSinh.setTen(ten.trim());

            if (!isNullOrEmpty(ngaySinhStr)) {
                try {
                    thiSinh.setNgaySinh(LocalDate.parse(ngaySinhStr));
                } catch (Exception e) {
                }
            }

            thiSinh.setGioiTinh(gioiTinh);
            thiSinh.setDienThoai(dienThoai != null ? dienThoai.trim() : null);
            thiSinh.setEmail(email != null ? email.trim() : null);
            thiSinh.setNoiSinh(noiSinh != null ? noiSinh.trim() : null);

            if (!isNullOrEmpty(doituongIdStr)) {
                try {
                    DoiTuongUutien dt = doiTuongService.findById(Integer.parseInt(doituongIdStr));
                    thiSinh.setDoiTuongUutien(dt);
                } catch (Exception e) {
                    thiSinh.setDoiTuongUutien(null);
                }
            } else {
                thiSinh.setDoiTuongUutien(null);
            }

            if (!isNullOrEmpty(khuvucIdStr)) {
                try {
                    KhuVucUutien kv = khuVucService.findById(Integer.parseInt(khuvucIdStr));
                    thiSinh.setKhuVucUutien(kv);
                } catch (Exception e) {
                    thiSinh.setKhuVucUutien(null);
                }
            } else {
                thiSinh.setKhuVucUutien(null);
            }

            thiSinhService.update(thiSinh);

            setMessage(request, "Cap nhat thong tin thanh cong!", "success");
            redirect(response, request.getContextPath() + "/dashboard");

        } catch (Exception e) {
            setMessage(request, "Da xay ra loi khi cap nhat thong tin: " + e.getMessage(), "danger");
            redirect(response, request.getContextPath() + "/profile");
        }
    }
}
