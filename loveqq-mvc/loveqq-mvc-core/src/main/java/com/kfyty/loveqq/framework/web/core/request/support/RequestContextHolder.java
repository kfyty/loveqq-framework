package com.kfyty.loveqq.framework.web.core.request.support;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 13:32
 * @email kfyty725@hotmail.com
 */
public class RequestContextHolder {
    private static final ThreadLocal<ServerRequest> REQUEST_LOCAL = new ThreadLocal<>();

    public static ServerRequest get() {
        ServerRequest request = REQUEST_LOCAL.get();
        if(request == null) {
            throw new IllegalStateException("The current thread is not bound to request !");
        }
        return request;
    }

    public static ServerRequest set(ServerRequest request) {
        ServerRequest prev = REQUEST_LOCAL.get();
        REQUEST_LOCAL.set(request);
        return prev;
    }
}
