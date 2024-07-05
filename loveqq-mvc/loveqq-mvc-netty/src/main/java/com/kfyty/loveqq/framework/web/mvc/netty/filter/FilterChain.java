package com.kfyty.loveqq.framework.web.mvc.netty.filter;

import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

/**
 * 描述: 过滤器链
 *
 * @author kfyty725
 * @date 2024/7/5 11:05
 * @email kfyty725@hotmail.com
 */
public interface FilterChain {
    /**
     * 过滤
     *
     * @param request  请求
     * @param response 响应
     */
    void doFilter(HttpServerRequest request, HttpServerResponse response);
}
