package com.kfyty.loveqq.framework.web.core.handler;

import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;

/**
 * 描述: 控制器异常处理器
 *
 * @author kfyty725
 * @date 2024/7/22 19:37
 * @email kfyty725@hotmail.com
 */
public interface ExceptionHandler {
    /**
     * 是否可以处理
     *
     * @param mapping   路由
     * @param throwable 异常
     * @return true if can handle this exception
     */
    boolean canHandle(MethodMapping mapping, Throwable throwable);

    /**
     * 异常处理器
     * 如果 {@link this#canHandle(MethodMapping, Throwable)} 匹配通过，但该方法无法处理，应抛出原始入参异常
     *
     * @param request   请求
     * @param response  响应
     * @param mapping   路由
     * @param throwable 异常
     * @return 处理后的返回值以及返回值类型，将作为控制器返回值继续处理
     */
    Pair<MethodParameter, Object> handle(ServerRequest request, ServerResponse response, MethodMapping mapping, Throwable throwable) throws Throwable;
}
