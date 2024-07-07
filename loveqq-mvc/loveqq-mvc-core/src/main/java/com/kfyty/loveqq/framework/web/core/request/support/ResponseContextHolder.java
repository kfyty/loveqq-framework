package com.kfyty.loveqq.framework.web.core.request.support;

import com.kfyty.loveqq.framework.web.core.http.ServerResponse;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 13:32
 * @email kfyty725@hotmail.com
 */
public class ResponseContextHolder {
    private static final ThreadLocal<ServerResponse> RESPONSE_LOCAL = new ThreadLocal<>();

    public static ServerResponse get() {
        ServerResponse request = RESPONSE_LOCAL.get();
        if (request == null) {
            throw new IllegalStateException("The current thread is not bound to response !");
        }
        return request;
    }

    public static ServerResponse set(ServerResponse response) {
        ServerResponse prev = RESPONSE_LOCAL.get();
        RESPONSE_LOCAL.set(response);
        return prev;
    }
}
