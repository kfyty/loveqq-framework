package com.kfyty.loveqq.framework.web.core.interceptor;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.route.Route;

/**
 * 描述: 拦截器接口
 *
 * @author kfyty
 * @date 2021/5/30 17:48
 * @email kfyty725@hotmail.com
 */
public interface HandlerInterceptor {
    /**
     * 拦截器包含路径匹配规则，优先级低于 {@link this#excludes()}
     *
     * @return 包含匹配规则，null 表示默认应用
     */
    default String[] includes() {
        return null;
    }

    /**
     * 拦截器排除路径匹配规则，优先级高于 {@link this#includes()}
     *
     * @return 包含匹配规则，null 表示默认应用
     */
    default String[] excludes() {
        return null;
    }

    /**
     * 前置处理
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return true if continue
     */
    default boolean preHandle(ServerRequest request, ServerResponse response, Route handler) {
        return true;
    }

    /**
     * 后置处理
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @param retValue 返回值
     */
    default void postHandle(ServerRequest request, ServerResponse response, Route handler, Object retValue) {
    }

    /**
     * 完成处理
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @param ex       异常，可能为空
     */
    default void afterCompletion(ServerRequest request, ServerResponse response, Route handler, Throwable ex) {
    }
}
