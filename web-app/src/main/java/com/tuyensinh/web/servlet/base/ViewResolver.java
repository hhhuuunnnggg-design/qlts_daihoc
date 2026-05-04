package com.tuyensinh.web.servlet.base;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ViewResolver {

    private static final String VIEW_PREFIX = "/WEB-INF/views/";
    private static final String VIEW_SUFFIX = ".jsp";

    public ModelAndView view(String viewName) {
        return new ModelAndView(viewName);
    }

    public ModelAndView redirect(String path) {
        return new ModelAndView("redirect:" + path);
    }

    public void render(HttpServletRequest request, HttpServletResponse response, ModelAndView mav)
            throws ServletException, IOException {
        if (mav.getModel() != null) {
            for (var entry : mav.getModel().entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }
        }
        request.getRequestDispatcher(resolve(mav.getViewName())).forward(request, response);
    }

    public void renderRedirect(HttpServletRequest request, HttpServletResponse response, ModelAndView mav)
            throws ServletException, IOException {
        if (mav.isRedirect()) {
            String path = mav.getRedirectPath();
            response.sendRedirect(request.getContextPath() + path);
        } else {
            render(request, response, mav);
        }
    }

    public String resolve(String viewName) {
        return VIEW_PREFIX + viewName + VIEW_SUFFIX;
    }
}
