package com.kfyty.loveqq.framework.web.core.route.gateway;

import com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer.LoadBalanceChooser;
import com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer.ServerInstance;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnClass;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.route.GatewayRoute;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * 功能描述: 网关负载均衡过滤器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
@Component
@RequiredArgsConstructor
@Order(Order.HIGHEST_PRECEDENCE)
@ConditionalOnClass("com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer.LoadBalanceChooser")
public class LoadBalanceGatewayFilter implements GatewayFilter {
    /**
     * 负载均衡协议
     */
    public static final String LOAD_BALANCE_SCHEME = "lb";

    /**
     * 负载均衡选择器
     */
    private final LoadBalanceChooser loadBalanceChooser;

    @Override
    public Mono<Void> doFilter(ServerRequest request, ServerResponse response, GatewayFilterChain chain) {
        GatewayRoute route = (GatewayRoute) request.getAttribute(GatewayRoute.GATEWAY_ROUTE_ATTRIBUTE);

        URI uri = route.getURI();

        if (!LOAD_BALANCE_SCHEME.equals(uri.getScheme())) {
            return chain.doFilter(request, response);
        }

        String host = uri.getHost();

        if (host == null) {
            String schemeSpecificPart = uri.getSchemeSpecificPart();                                // 可能是 ws，尝试从 SchemeSpecificPart 获取
            if (schemeSpecificPart != null) {
                uri = URI.create(schemeSpecificPart);
                host = uri.getHost();
            }
            if (host == null) {
                return Mono.just(response).doOnNext(e -> e.setStatus(503)).then();                  // 仍然获取不到，返回 503
            }
        }

        ServerInstance server = this.loadBalanceChooser.choose(host);

        if (server == null) {
            return Mono.just(response).doOnNext(e -> e.setStatus(503)).then();
        }

        String routeURI = this.resolveRouteURI(uri, request, server);
        route.setUri(URI.create(routeURI));

        return chain.doFilter(request, response);
    }

    protected String resolveRouteURI(URI uri, ServerRequest request, ServerInstance server) {
        final String scheme = uri.getScheme().toLowerCase();
        if (scheme.equals("ws") || scheme.equals("wss")) {
            return scheme + "://" + server.getIp() + ':' + server.getPort();
        }
        if (UPGRADE.equalsIgnoreCase(request.getHeader("connection"))) {
            if (request.getScheme().equals("http")) {
                return "ws://" + server.getIp() + ':' + server.getPort();
            }
            return "wss://" + server.getIp() + ':' + server.getPort();
        }
        return server.getScheme() + "://" + server.getIp() + ':' + server.getPort();
    }
}
