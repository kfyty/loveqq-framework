package com.kfyty.loveqq.framework.boot.mvc.server.netty.http.filter;

import com.kfyty.loveqq.framework.boot.mvc.server.netty.http.NettyServerResponse;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.route.GatewayRoute;
import com.kfyty.loveqq.framework.web.core.route.gateway.GatewayFilter;
import com.kfyty.loveqq.framework.web.core.route.gateway.GatewayFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

/**
 * 功能描述: 抽象的 netty 路由过滤器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
public abstract class AbstractNettyRouteGatewayFilter implements GatewayFilter {

    @Override
    public Mono<Void> doFilter(ServerRequest request, ServerResponse response, GatewayFilterChain chain) {
        if (response instanceof NettyServerResponse) {
            final GatewayRoute route = (GatewayRoute) request.getAttribute(GatewayRoute.GATEWAY_ROUTE_ATTRIBUTE);
            return this.doFilter(route, request, response, chain);
        }
        return chain.doFilter(request, response);
    }

    protected abstract Mono<Void> doFilter(GatewayRoute route, ServerRequest request, ServerResponse response, GatewayFilterChain chain);

    protected URI createRouteURI(GatewayRoute route, ServerRequest request) {
        URI routeURI = route.getURI();
        StringBuilder newRouteURI = new StringBuilder(routeURI.getScheme())
                .append("://")
                .append(routeURI.getHost())
                .append(':')
                .append(routeURI.getPort())
                .append(request.getRequestURI());

        // 此时没有接受请求体，都是查询参数
        Map<String, String> parameters = request.getParameterMap();
        if (!parameters.isEmpty()) {
            newRouteURI.append('?');
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                newRouteURI.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
            }
            newRouteURI.deleteCharAt(newRouteURI.length() - 1);
        }

        return URI.create(newRouteURI.toString());
    }
}
