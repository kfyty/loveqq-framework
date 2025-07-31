package com.kfyty.loveqq.framework.web.core.mapping;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;

/**
 * 描述: 路由匹配器
 *
 * @author kfyty725
 * @date 2021/6/4 10:05
 * @email kfyty725@hotmail.com
 */
public interface RouteMatcher {
    /**
     * 匹配路由
     *
     * @param method     请求方法
     * @param request    请求
     * @return {@link Route}
     */
    Route match(RequestMethod method, ServerRequest request);
}
