package com.tuyensinh.web.servlet.base;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {

    private static final String REDIRECT_PREFIX = "redirect:";

    private final String viewName;
    private final Map<String, Object> model = new HashMap<>();

    public ModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public ModelAndView addObject(String key, Object value) {
        model.put(key, value);
        return this;
    }

    public String getViewName() { return viewName; }
    public Map<String, Object> getModel() { return model; }

    public boolean isRedirect() {
        return viewName != null && viewName.startsWith(REDIRECT_PREFIX);
    }

    public String getRedirectPath() {
        if (!isRedirect()) return null;
        return viewName.substring(REDIRECT_PREFIX.length());
    }
}
