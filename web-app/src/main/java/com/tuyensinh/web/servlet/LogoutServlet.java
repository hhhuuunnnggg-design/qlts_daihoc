package com.tuyensinh.web.servlet;

import com.tuyensinh.web.servlet.base.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LogoutServlet extends BaseServlet {

    @Override
    protected ModelAndView handleGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        authController.logout(request);
        setMessage(request, "Bạn đã đăng xuất thành công.", "success");
        return viewResolver.redirect("/login");
    }

    @Override
    protected ModelAndView handlePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        return handleGet(request, response);
    }
}
