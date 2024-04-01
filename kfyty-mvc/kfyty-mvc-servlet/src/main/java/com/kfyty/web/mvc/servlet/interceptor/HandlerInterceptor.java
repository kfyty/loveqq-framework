package com.kfyty.web.mvc.servlet.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 描述: 拦截器接口
 *
 * @author fyty
 * @date 2021/5/30 17:48
 * @email kfyty725@hotmail.com
 */
public interface HandlerInterceptor {

    default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, Object retValue) throws Exception {
    }

    default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Throwable ex) throws Exception {
    }
}
