package com.kfyty.loveqq.framework.web.core.route.gateway;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.route.GatewayRoute;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

/**
 * 功能描述: 网关路由过滤器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
public interface GatewayFilter {
    /**
     * web socket 升级
     */
    String UPGRADE = "upgrade";

    /**
     * 过滤
     *
     * @param request  请求
     * @param response 响应
     * @param chain    过滤器链
     */
    default Mono<Void> doFilter(ServerRequest request, ServerResponse response, GatewayFilterChain chain) {
        GatewayRoute route = (GatewayRoute) request.getAttribute(GatewayRoute.GATEWAY_ROUTE_ATTRIBUTE);
        return this.doFilter(route, request, response, chain);
    }

    /**
     * 过滤
     *
     * @param route    路由
     * @param request  请求
     * @param response 响应
     * @param chain    过滤器链
     */
    default Mono<Void> doFilter(GatewayRoute route, ServerRequest request, ServerResponse response, GatewayFilterChain chain) {
        return chain.doFilter(request, response);
    }

    /**
     * 获取配置 class
     *
     * @return 配置 class
     */
    default Class<?> getConfigClass() {
        return null;
    }

    /**
     * 设置配置
     *
     * @param config   配置
     * @param metadata 配置元数据
     */
    default void setConfig(Object config, Map<String, String> metadata) {

    }

    /**
     * 是否是 websocket 连接
     *
     * @param request  请求
     * @param response 响应
     * @return true if websocket
     */
    default boolean isWebSocket(ServerRequest request, ServerResponse response) {
        return UPGRADE.equalsIgnoreCase(request.getHeader("connection"));
    }

    /**
     * 创建路由 URI
     *
     * @param route   路由
     * @param request 请求
     * @return 路由目标 URI
     */
    default URI createRouteURI(GatewayRoute route, ServerRequest request) {
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
