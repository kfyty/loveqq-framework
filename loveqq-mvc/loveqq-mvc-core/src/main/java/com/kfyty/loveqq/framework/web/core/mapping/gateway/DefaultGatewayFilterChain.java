package com.kfyty.loveqq.framework.web.core.mapping.gateway;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 功能描述: 网关路由过滤器链
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
@RequiredArgsConstructor
public class DefaultGatewayFilterChain implements GatewayFilterChain {
    /**
     * 过滤器
     */
    private final List<GatewayFilter> filters;

    /**
     * 当前的索引
     */
    private int index;

    @Override
    public Mono<Void> doFilter(ServerRequest request, ServerResponse response) {
        if (this.filters == null || this.index >= this.filters.size()) {
            return Mono.empty();
        }
        return this.filters.get(this.index++).doFilter(request, response, this);
    }
}
