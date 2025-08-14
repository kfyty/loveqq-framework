package com.kfyty.loveqq.framework.web.core.route.gateway;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * 功能描述: 网关路由过滤器链
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
public interface GatewayFilterChain {
    /**
     * 过滤
     *
     * @param request  请求
     * @param response 响应
     */
    Mono<Void> doFilter(ServerRequest request, ServerResponse response);
}
