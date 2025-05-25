package com.kfyty.loveqq.framework.web.core.filter;

import com.kfyty.loveqq.framework.web.core.filter.internal.FilterTransformer;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import org.reactivestreams.Publisher;
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
     * 如果是基于 servlet，需要重新该方法返回 /* 形式
     *
     * @return 匹配路径
     */
    default String[] getPattern() {
        return new String[]{"/**"};
    }

    /**
     * 返回是否 websocket 请求
     *
     * @param request 请求
     * @return true if websocket
     */
    default boolean isWebSocket(ServerRequest request) {
        String connection = request.getHeader("connection");
        return connection != null && connection.equalsIgnoreCase("upgrade");
    }

    /**
     * 过滤
     *
     * @param request  请求
     * @param response 响应
     * @param chain    过滤器链
     */
    default Publisher<Void> doFilter(ServerRequest request, ServerResponse response, FilterChain chain) {
        return Mono.fromSupplier(() -> doFilter(request, response)).flatMap(new FilterTransformer(request, response, chain));
    }

    /**
     * 过滤，主要用于适配 servlet filter
     *
     * @param request  请求
     * @param response 响应
     * @return true if continue filter
     */
    default boolean doFilter(ServerRequest request, ServerResponse response) {
        return true;
    }
}
