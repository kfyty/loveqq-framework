package com.kfyty.loveqq.framework.boot.mvc.server.netty.http.filter;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.route.GatewayRoute;
import com.kfyty.loveqq.framework.web.core.route.gateway.GatewayFilter;
import com.kfyty.loveqq.framework.web.core.route.gateway.GatewayFilterChain;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.util.Map;

/**
 * 功能描述: 实际的转发路由过滤器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
@RequiredArgsConstructor
@Order(Order.LOWEST_PRECEDENCE)
@Component(GatewayRoute.DEFAULT_FORWARD_FILTER_NAME)
public class ForwardRouteGatewayFilter implements GatewayFilter {
    /**
     * 默认的 http client
     */
    private final HttpClient client;

    @Override
    public Mono<Void> doFilter(GatewayRoute route, ServerRequest request, ServerResponse response, GatewayFilterChain chain) {
        if (this.isWebSocket(request, response)) {
            return chain.doFilter(request, response);
        }
        final URI routeURI = this.createRouteURI(route, request);
        return this.client.headers(headers -> {
                    for (String headerName : request.getHeaderNames()) {
                        headers.add(headerName, request.getHeaders(headerName));
                    }
                })
                .request(HttpMethod.valueOf(request.getMethod()))
                .uri(routeURI)
                .send((req, outbound) -> outbound.send(request.getBody().map(ByteBuf::retain)))
                .responseConnection((res, connection) -> {
                    response.setStatus(res.status().code());
                    for (Map.Entry<String, String> entry : res.responseHeaders().entries()) {
                        response.addHeader(entry.getKey(), entry.getValue());
                    }
                    return response.writeBody(connection.inbound().receive().retain())
                            .then(chain.doFilter(request, response))
                            .then(Mono.defer(response::sendBody))
                            .doFinally(s -> connection.dispose());
                })
                .then();
    }
}
