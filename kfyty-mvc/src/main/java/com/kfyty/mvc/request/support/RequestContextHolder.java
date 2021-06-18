package com.kfyty.mvc.request.support;

import javax.servlet.http.HttpServletRequest;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 13:32
 * @email kfyty725@hotmail.com
 */
public class RequestContextHolder {
    private static final ThreadLocal<HttpServletRequest> servletRequestLocal = new ThreadLocal<>();

    public static void setCurrentRequest(HttpServletRequest request) {
        servletRequestLocal.set(request);
    }

    public static HttpServletRequest getCurrentRequest() {
        HttpServletRequest request = servletRequestLocal.get();
        if(request == null) {
            throw new IllegalStateException("the current thread is not bound to request !");
        }
        return request;
    }

    public static void removeCurrentRequest() {
        servletRequestLocal.remove();
    }
}
