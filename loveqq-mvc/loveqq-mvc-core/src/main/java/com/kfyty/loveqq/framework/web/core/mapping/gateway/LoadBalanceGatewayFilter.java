package com.kfyty.loveqq.framework.web.core.mapping.gateway;

import com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer.LoadBalanceChooser;
import com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer.ServerInstance;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnClass;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.mapping.GatewayRoute;
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
    private static final String LOAD_BALANCE_SCHEME = "lb";

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

        ServerInstance server = this.loadBalanceChooser.choose(uri.getHost());
        if (server == null) {
            return Mono.just(response).doOnNext(e -> e.setStatus(503)).then();
        }

        String newRouteURI = server.getScheme() + "://" + server.getIp() + ':' + server.getPort();
        route.setUri(URI.create(newRouteURI));
        return chain.doFilter(request, response);
    }
}
