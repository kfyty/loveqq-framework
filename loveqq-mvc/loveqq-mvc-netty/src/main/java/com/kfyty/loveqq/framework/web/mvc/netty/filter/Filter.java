package com.kfyty.loveqq.framework.web.mvc.netty.filter;

import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

/**
 * 描述: netty 过滤器
 *
 * @author kfyty725
 * @date 2024/7/5 11:04
 * @email kfyty725@hotmail.com
 */
public interface Filter {
    /**
     * 返回匹配路径
     *
     * @return 匹配路径
     */
    default String[] getPattern() {
        return new String[]{"/*"};
    }

    /**
     * 过滤
     *
     * @param request  请求
     * @param response 响应
     * @param chain    过滤器链
     */
    void doFilter(HttpServerRequest request, HttpServerResponse response, FilterChain chain);
}
