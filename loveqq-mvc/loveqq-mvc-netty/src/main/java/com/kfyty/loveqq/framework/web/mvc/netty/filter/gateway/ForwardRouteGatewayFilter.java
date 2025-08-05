package com.kfyty.loveqq.framework.web.mvc.netty.filter.gateway;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.mapping.GatewayRoute;
import com.kfyty.loveqq.framework.web.core.mapping.gateway.GatewayFilterChain;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServerResponse;

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
public class ForwardRouteGatewayFilter extends AbstractNettyRouteGatewayFilter {
    /**
     * 默认的 http client
     */
    private final HttpClient client;

    @Override
    public Mono<Void> doFilter(GatewayRoute route, ServerRequest request, ServerResponse response, GatewayFilterChain chain) {
        final URI routeURI = this.createRouteURI(route, request);
        final Flux<Void> flux = this.client.headers(headers -> {
                    for (String headerName : request.getHeaderNames()) {
                        headers.add(headerName, request.getHeaders(headerName));
                    }
                })
                .request(HttpMethod.valueOf(request.getMethod()))
                .uri(routeURI)
                .send((req, outbound) -> outbound.send(request.getBody().map(ByteBuf::retain)))
                .responseConnection((res, connection) -> {
                    HttpServerResponse nettyResponse = response.getRawResponse();
                    nettyResponse.status(res.status());
                    for (Map.Entry<String, String> entry : res.responseHeaders().entries()) {
                        nettyResponse.addHeader(entry.getKey(), entry.getValue());
                    }
                    return Mono.from(nettyResponse.send(connection.inbound().receive().retain())).doFinally(s -> connection.dispose());
                });
        return chain.doFilter(request, response).then(flux.then());
    }
}
