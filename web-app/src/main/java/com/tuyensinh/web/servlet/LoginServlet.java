package com.tuyensinh.web.servlet;

import com.tuyensinh.entity.NguoiDung;
import com.tuyensinh.service.AuthService;
import com.tuyensinh.web.servlet.base.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class LoginServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected ModelAndView handleGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (authController.isLoggedIn(request)) {
            return viewResolver.redirect("/dashboard");
        }
        return viewResolver.view("login");
    }

    @Override
    protected ModelAndView handlePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = trim(request.getParameter("username"));
        String password = trim(request.getParameter("password"));

        if (username == null || password == null) {
            setMessage(request, "Vui lòng nhập đầy đủ thông tin đăng nhập.", "danger");
            return viewResolver.view("login");
        }

        try {
            Optional<NguoiDung> userOpt = authService.login(username, password);

            if (userOpt.isPresent()) {
                NguoiDung nguoiDung = userOpt.get();
                authController.login(request, nguoiDung);

                String msg = nguoiDung.isAdmin()
                    ? "Đăng nhập thành công! Xin chào admin."
                    : "Đăng nhập thành công! Xin chào " + nguoiDung.getHoTen() + ".";
                setMessage(request, msg, "success");
                return viewResolver.redirect("/dashboard");
            } else {
                setMessage(request, "Tên đăng nhập hoặc mật khẩu không đúng.", "danger");
                return viewResolver.view("login");
            }
        } catch (Exception e) {
            setMessage(request, "Đã xảy ra lỗi trong quá trình đăng nhập: " + e.getMessage(), "danger");
            return viewResolver.view("login");
        }
    }

    private String trim(String v) { return v != null ? v.trim() : null; }
}
