package com.kfyty.loveqq.framework.web.mvc.netty.interceptor;

import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

/**
 * 描述: 拦截器接口
 *
 * @author kfyty
 * @date 2021/5/30 17:48
 * @email kfyty725@hotmail.com
 */
public interface HandlerInterceptor {

    default boolean preHandle(HttpServerRequest request, HttpServerResponse response, Object handler) throws Exception {
        return true;
    }

    default void postHandle(HttpServerRequest request, HttpServerResponse response, Object handler, Object retValue) throws Exception {
    }

    default void afterCompletion(HttpServerRequest request, HttpServerResponse response, Object handler, Throwable ex) throws Exception {
    }
}
