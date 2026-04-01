package com.tuyensinh.web.servlet;

import com.google.gson.Gson;
import com.tuyensinh.entity.NguoiDung;
import com.tuyensinh.util.HibernateUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseServlet extends HttpServlet {

    protected Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        handleGet(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        handlePost(request, response);
    }

    protected abstract void handleGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;

    protected abstract void handlePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;

    protected void requireLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoidung") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    protected NguoiDung getLoggedInUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (NguoiDung) session.getAttribute("nguoidung");
        }
        return null;
    }

    protected void setSessionAttribute(HttpServletRequest request, String key, Object value) {
        HttpSession session = request.getSession(true);
        session.setAttribute(key, value);
    }

    protected Object getSessionAttribute(HttpServletRequest request, String key) {
        HttpSession session = request.getSession(false);
        return session != null ? session.getAttribute(key) : null;
    }

    protected void removeSessionAttribute(HttpServletRequest request, String key) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(key);
        }
    }

    protected void setAttribute(HttpServletRequest request, String key, Object value) {
        request.setAttribute(key, value);
    }

    protected void setMessage(HttpServletRequest request, String message, String type) {
        setSessionAttribute(request, "message", message);
        setSessionAttribute(request, "messageType", type != null ? type : "info");
    }

    protected void forward(HttpServletRequest request, HttpServletResponse response, String viewPath)
            throws ServletException, IOException {
        request.getRequestDispatcher(viewPath).forward(request, response);
    }

    protected void redirect(HttpServletResponse response, String path) throws IOException {
        response.sendRedirect(path);
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

    protected String getViewPath(String viewName) {
        return "/WEB-INF/views/" + viewName + ".jsp";
    }

    protected String getParameter(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        return value != null && !value.trim().isEmpty() ? value.trim() : defaultValue;
    }

    protected Integer getIntParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    protected Short getShortParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Short.parseShort(value.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    protected boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
