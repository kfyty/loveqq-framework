package com.kfyty.loveqq.framework.web.mvc.netty.filter;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import reactor.core.publisher.Mono;

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
     * 返回是否 websocket 请求
     *
     * @param request 请求
     * @return true if websocket
     */
    default boolean isWebSocket(ServerRequest request) {
        String connection = request.getHeader(HttpHeaderNames.CONNECTION.toString());
        return connection != null && connection.equalsIgnoreCase(HttpHeaderValues.UPGRADE.toString());
    }

    /**
     * 过滤
     *
     * @param request  请求
     * @param response 响应
     * @param chain    过滤器链
     */
    Mono<Void> doFilter(ServerRequest request, ServerResponse response, FilterChain chain);
}
