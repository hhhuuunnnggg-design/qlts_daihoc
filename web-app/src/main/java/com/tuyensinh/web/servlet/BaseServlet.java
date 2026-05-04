package com.tuyensinh.web.servlet;

import com.google.gson.Gson;
import com.tuyensinh.web.servlet.base.AuthController;
import com.tuyensinh.web.servlet.base.ModelAndView;
import com.tuyensinh.web.servlet.base.ViewResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseServlet extends HttpServlet {

    protected final Gson gson = new Gson();
    protected final ViewResolver viewResolver = new ViewResolver();
    protected final AuthController authController = new AuthController();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        ModelAndView mav = handleGet(request, response);
        if (mav != null) {
            viewResolver.renderRedirect(request, response, mav);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        ModelAndView mav = handlePost(request, response);
        if (mav != null) {
            viewResolver.renderRedirect(request, response, mav);
        }
    }

    protected abstract ModelAndView handleGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;

    protected abstract ModelAndView handlePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;

    protected void setMessage(HttpServletRequest request, String message, String type) {
        authController.setFlashMessage(request, message, type);
    }

    protected void sendJson(HttpServletResponse response, Map<String, Object> data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }

    protected void sendJson(HttpServletResponse response, String json) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }

    protected Map<String, Object> successResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }

    protected Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
