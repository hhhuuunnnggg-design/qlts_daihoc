package com.tuyensinh.web.servlet;

import com.tuyensinh.entity.NguoiDung;
import com.tuyensinh.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

public class LoginServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("nguoidung") != null) {
            redirect(response, request.getContextPath() + "/dashboard");
            return;
        }
        forward(request, response, getViewPath("login"));
    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (isNullOrEmpty(username) || isNullOrEmpty(password)) {
            setMessage(request, "Vui lòng nhập đầy đủ thông tin đăng nhập.", "danger");
            forward(request, response, getViewPath("login"));
            return;
        }

        try {
            Optional<NguoiDung> userOpt = authService.login(username, password);

            if (userOpt.isPresent()) {
                NguoiDung nguoiDung = userOpt.get();
                HttpSession session = request.getSession(true);
                session.setAttribute("nguoidung", nguoiDung);
                session.setMaxInactiveInterval(30 * 60);

                if (nguoiDung.isAdmin()) {
                    setMessage(request, "Đăng nhập thành công! Xin chào admin.", "success");
                } else {
                    setMessage(request, "Đăng nhập thành công! Xin chào " + nguoiDung.getHoTen() + ".", "success");
                }
                redirect(response, request.getContextPath() + "/dashboard");
            } else {
                setMessage(request, "Tên đăng nhập hoặc mật khẩu không đúng.", "danger");
                forward(request, response, getViewPath("login"));
            }
        } catch (Exception e) {
            setMessage(request, "Đã xảy ra lỗi trong quá trình đăng nhập: " + e.getMessage(), "danger");
            forward(request, response, getViewPath("login"));
        }
    }
}
