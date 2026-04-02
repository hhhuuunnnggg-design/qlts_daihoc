package com.tuyensinh.web.servlet;

import com.tuyensinh.entity.DoiTuongUutien;
import com.tuyensinh.entity.KhuVucUutien;
import com.tuyensinh.entity.NguoiDung;
import com.tuyensinh.entity.ThiSinh;
import com.tuyensinh.entity.VaiTro;
import com.tuyensinh.service.AuthService;
import com.tuyensinh.service.DoiTuongService;
import com.tuyensinh.service.KhuVucService;
import com.tuyensinh.service.ThiSinhService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class RegisterServlet extends BaseServlet {

    private final AuthService authService = new AuthService();
    private final ThiSinhService thiSinhService = new ThiSinhService();
    private final DoiTuongService doiTuongService = new DoiTuongService();
    private final KhuVucService khuVucService = new KhuVucService();

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("nguoidung") != null) {
            redirect(response, request.getContextPath() + "/dashboard");
            return;
        }

        try {
            List<DoiTuongUutien> danhSachDoiTuong = doiTuongService.findAll();
            List<KhuVucUutien> danhSachKhuVuc = khuVucService.findAll();

            setAttribute(request, "danhSachDoiTuong", danhSachDoiTuong);
            setAttribute(request, "danhSachKhuVuc", danhSachKhuVuc);

            forward(request, response, getViewPath("register"));
        } catch (Exception e) {
            setMessage(request, "Đã xảy ra lỗi: " + e.getMessage(), "danger");
            forward(request, response, getViewPath("register"));
        }
    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String ho = request.getParameter("ho");
        String ten = request.getParameter("ten");
        String cccd = request.getParameter("cccd");
        String ngaySinhStr = request.getParameter("ngaySinh");
        String gioiTinh = request.getParameter("gioiTinh");
        String dienThoai = request.getParameter("dienThoai");
        String email = request.getParameter("email");
        String noiSinh = request.getParameter("noiSinh");
        String doituongIdStr = request.getParameter("doituongId");
        String khuvucIdStr = request.getParameter("khuvucId");

        if (isNullOrEmpty(username) || isNullOrEmpty(password) || isNullOrEmpty(confirmPassword) ||
            isNullOrEmpty(ho) || isNullOrEmpty(ten) || isNullOrEmpty(cccd)) {
            setMessage(request, "Vui lòng nhập đầy đủ thông tin bắt buộc.", "danger");
            redirectToRegister(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            setMessage(request, "Mật khẩu xác nhận không khớp.", "danger");
            redirectToRegister(request, response);
            return;
        }

        if (password.length() < 6) {
            setMessage(request, "Mật khẩu phải có ít nhất 6 ký tự.", "danger");
            redirectToRegister(request, response);
            return;
        }

        try {
            if (thiSinhService.findByCccd(cccd).isPresent()) {
                setMessage(request, "Số CCCD đã được đăng ký.", "danger");
                redirectToRegister(request, response);
                return;
            }

            VaiTro vaiTro = new VaiTro();
            vaiTro.setVaitroId((short) 2);

            NguoiDung nguoiDung = new NguoiDung();
            nguoiDung.setUsername(username);
            nguoiDung.setHoTen(ho + " " + ten);
            nguoiDung.setEmail(email);
            nguoiDung.setVaiTro(vaiTro);
            nguoiDung.setIsActive(true);

            NguoiDung savedNguoiDung = authService.register(nguoiDung, password);

            ThiSinh thiSinh = new ThiSinh();
            thiSinh.setNguoiDung(savedNguoiDung);
            thiSinh.setCccd(cccd);
            thiSinh.setHo(ho);
            thiSinh.setTen(ten);
            thiSinh.setGioiTinh(gioiTinh);
            thiSinh.setDienThoai(dienThoai);
            thiSinh.setEmail(email);
            thiSinh.setNoiSinh(noiSinh);

            if (ngaySinhStr != null && !ngaySinhStr.isEmpty()) {
                thiSinh.setNgaySinh(LocalDate.parse(ngaySinhStr));
            }

            if (doituongIdStr != null && !doituongIdStr.isEmpty()) {
                DoiTuongUutien dt = doiTuongService.findById(Integer.parseInt(doituongIdStr));
                thiSinh.setDoiTuongUutien(dt);
            }

            if (khuvucIdStr != null && !khuvucIdStr.isEmpty()) {
                KhuVucUutien kv = khuVucService.findById(Integer.parseInt(khuvucIdStr));
                thiSinh.setKhuVucUutien(kv);
            }

            thiSinhService.save(thiSinh);

            HttpSession session = request.getSession(true);
            session.setAttribute("nguoidung", savedNguoiDung);
            session.setMaxInactiveInterval(30 * 60);

            setMessage(request, "Đăng ký thành công! Xin chào " + ho + " " + ten + ".", "success");
            redirect(response, request.getContextPath() + "/dashboard");

        } catch (Exception e) {
            setMessage(request, "Đã xảy ra lỗi trong quá trình đăng ký: " + e.getMessage(), "danger");
            redirectToRegister(request, response);
        }
    }

    private void redirectToRegister(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            List<DoiTuongUutien> danhSachDoiTuong = doiTuongService.findAll();
            List<KhuVucUutien> danhSachKhuVuc = khuVucService.findAll();
            setAttribute(request, "danhSachDoiTuong", danhSachDoiTuong);
            setAttribute(request, "danhSachKhuVuc", danhSachKhuVuc);
        } catch (Exception e) {
        }
        redirect(response, request.getContextPath() + "/register");
    }
}
