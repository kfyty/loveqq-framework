package com.kfyty.loveqq.framework.web.mvc.netty.request.support;

import reactor.netty.http.server.HttpServerResponse;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 13:32
 * @email kfyty725@hotmail.com
 */
public class ResponseContextHolder {
    private static final ThreadLocal<HttpServerResponse> serverResponseLocal = new ThreadLocal<>();

    public static void setCurrentResponse(HttpServerResponse response) {
        serverResponseLocal.set(response);
    }

    public static HttpServerResponse getCurrentResponse() {
        HttpServerResponse request = serverResponseLocal.get();
        if(request == null) {
            throw new IllegalStateException("the current thread is not bound to response !");
        }
        return request;
    }

    public static void removeCurrentResponse() {
        serverResponseLocal.remove();
    }
}
