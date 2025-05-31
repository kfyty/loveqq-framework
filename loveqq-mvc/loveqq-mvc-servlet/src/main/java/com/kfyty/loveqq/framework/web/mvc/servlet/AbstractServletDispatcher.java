package com.kfyty.loveqq.framework.web.mvc.servlet;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.AbstractDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * 描述: servlet 基础实现
 *
 * @author kfyty725
 * @date 2024/7/7 9:04
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractServletDispatcher<T extends AbstractServletDispatcher<T>> extends AbstractDispatcher<T> implements Servlet, ServletConfig {
    private static final String LSTRING_FILE = "jakarta.servlet.LocalStrings";
    private static final ResourceBundle lStrings = ResourceBundle.getBundle(LSTRING_FILE);

    private transient ServletConfig config;

    @Override
    public String getServletName() {
        ServletConfig sc = getServletConfig();
        if (sc == null) {
            throw new IllegalStateException(lStrings.getString("err.servlet_config_not_initialized"));
        }
        return sc.getServletName();
    }

    @Override
    public ServletContext getServletContext() {
        ServletConfig sc = getServletConfig();
        if (sc == null) {
            throw new IllegalStateException(lStrings.getString("err.servlet_config_not_initialized"));
        }
        return sc.getServletContext();
    }

    @Override
    public String getInitParameter(String name) {
        ServletConfig sc = getServletConfig();
        if (sc == null) {
            throw new IllegalStateException(lStrings.getString("err.servlet_config_not_initialized"));
        }
        return sc.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        ServletConfig sc = getServletConfig();
        if (sc == null) {
            throw new IllegalStateException(lStrings.getString("err.servlet_config_not_initialized"));
        }
        return sc.getInitParameterNames();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        this.init();
    }

    public void init() throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return this.config;
    }

    @Override
    public String getServletInfo() {
        return CommonUtil.EMPTY_STRING;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        if (!(req instanceof HttpServletRequest && res instanceof HttpServletResponse)) {
            throw new ServletException("Non-HTTP request or response");
        }
        this.service((HttpServletRequest) req, (HttpServletResponse) res);
    }

    public abstract void service(HttpServletRequest request, HttpServletResponse response) throws ServletException;

    @Override
    public void destroy() {

    }
}
