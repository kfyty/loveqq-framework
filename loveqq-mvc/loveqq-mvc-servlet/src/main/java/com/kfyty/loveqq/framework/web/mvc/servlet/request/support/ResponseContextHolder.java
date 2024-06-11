package com.kfyty.loveqq.framework.web.mvc.servlet.request.support;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 13:32
 * @email kfyty725@hotmail.com
 */
public class ResponseContextHolder {
    private static final ThreadLocal<HttpServletResponse> servletResponseLocal = new ThreadLocal<>();

    public static void setCurrentResponse(HttpServletResponse response) {
        servletResponseLocal.set(response);
    }

    public static HttpServletResponse getCurrentResponse() {
        HttpServletResponse request = servletResponseLocal.get();
        if(request == null) {
            throw new IllegalStateException("the current thread is not bound to response !");
        }
        return request;
    }

    public static void removeCurrentResponse() {
        servletResponseLocal.remove();
    }
}
