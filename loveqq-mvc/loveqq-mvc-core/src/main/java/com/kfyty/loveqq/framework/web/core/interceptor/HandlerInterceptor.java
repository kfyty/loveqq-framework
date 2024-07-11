package com.kfyty.loveqq.framework.web.core.interceptor;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;

/**
 * 描述: 拦截器接口
 *
 * @author kfyty
 * @date 2021/5/30 17:48
 * @email kfyty725@hotmail.com
 */
public interface HandlerInterceptor {

    default boolean preHandle(ServerRequest request, ServerResponse response, MethodMapping handler) {
        return true;
    }

    default void postHandle(ServerRequest request, ServerResponse response, MethodMapping handler, Object retValue) {
    }

    default void afterCompletion(ServerRequest request, ServerResponse response, MethodMapping handler, Throwable ex) {
    }
}
