package com.kfyty.loveqq.framework.web.mvc.netty.request.support;

import reactor.netty.http.server.HttpServerRequest;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 13:32
 * @email kfyty725@hotmail.com
 */
public class RequestContextHolder {
    private static final ThreadLocal<HttpServerRequest> serverRequestLocal = new ThreadLocal<>();

    public static void setCurrentRequest(HttpServerRequest request) {
        serverRequestLocal.set(request);
    }

    public static HttpServerRequest getCurrentRequest() {
        HttpServerRequest request = serverRequestLocal.get();
        if(request == null) {
            throw new IllegalStateException("the current thread is not bound to request !");
        }
        return request;
    }

    public static void removeCurrentRequest() {
        serverRequestLocal.remove();
    }
}
