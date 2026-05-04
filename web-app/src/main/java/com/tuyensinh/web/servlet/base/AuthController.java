package com.tuyensinh.web.servlet.base;

import com.tuyensinh.entity.NguoiDung;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthController {

    private static final String USER_SESSION_KEY = "nguoidung";
    private static final String FLASH_MESSAGE_KEY = "message";
    private static final String FLASH_MESSAGE_TYPE_KEY = "messageType";

    public ModelAndView requireLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (getCurrentUser(request) == null) {
            return redirect("/login");
        }
        return null;
    }

    public NguoiDung getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (NguoiDung) session.getAttribute(USER_SESSION_KEY);
        }
        return null;
    }

    public boolean isLoggedIn(HttpServletRequest request) {
        return getCurrentUser(request) != null;
    }

    public void login(HttpServletRequest request, NguoiDung user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_SESSION_KEY, user);
        session.setMaxInactiveInterval(30 * 60);
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public void setFlashMessage(HttpServletRequest request, String message, String type) {
        HttpSession session = request.getSession(true);
        session.setAttribute(FLASH_MESSAGE_KEY, message);
        session.setAttribute(FLASH_MESSAGE_TYPE_KEY, type != null ? type : "info");
    }

    public String getFlashMessage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? (String) session.getAttribute(FLASH_MESSAGE_KEY) : null;
    }

    public void clearFlashMessage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(FLASH_MESSAGE_KEY);
            session.removeAttribute(FLASH_MESSAGE_TYPE_KEY);
        }
    }

    private ModelAndView redirect(String path) {
        return new ModelAndView("redirect:" + path);
    }
}
