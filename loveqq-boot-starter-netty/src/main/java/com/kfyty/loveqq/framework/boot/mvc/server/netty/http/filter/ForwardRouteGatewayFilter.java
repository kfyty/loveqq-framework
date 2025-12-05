package com.kfyty.loveqq.framework.boot.mvc.server.netty.http.filter;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.request.support.RequestContextHolder;
import com.kfyty.loveqq.framework.web.core.route.GatewayRoute;
import com.kfyty.loveqq.framework.web.core.route.gateway.GatewayFilter;
import com.kfyty.loveqq.framework.web.core.route.gateway.GatewayFilterChain;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

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
        final Duration timeout = this.getResponseTimeout(route);
        final Flux<Void> flux = this.client.headers(headers -> {
                    String traceId = (String) request.getAttribute(RequestContextHolder.REQUEST_TRACE_ID_ATTRIBUTE);
                    if (traceId != null) {
                        headers.add(ConstantConfig.TRACK_ID, traceId);
                    }
                    for (String headerName : request.getHeaderNames()) {
                        headers.add(headerName, request.getHeaders(headerName));
                    }
                })
                .request(HttpMethod.valueOf(request.getMethod()))
                .uri(routeURI)
                .send((req, outbound) -> outbound.send(request.getBody().map(ByteBuf::retain)))
                .responseConnection((res, connection) -> {
                    response.setStatus(res.status().code());
                    request.setAttribute(GatewayRoute.CLIENT_CONNECTION_ATTRIBUTE, connection);
                    for (Map.Entry<String, String> entry : res.responseHeaders().entries()) {
                        response.addHeader(entry.getKey(), entry.getValue());
                    }
                    return response.writeBody(connection.inbound().receive().retain())
                            .then(chain.doFilter(request, response))
                            .then(Mono.defer(response::sendBody));
                });
        if (timeout == null) {
            return flux.then();
        }
        return flux.timeout(timeout, Mono.error(new TimeoutException("Gateway response timeout: " + timeout)))
                .doOnError(TimeoutException.class, ex -> response.setStatus(504))
                .doFinally(s -> this.cleanup(request, response))
                .then();
    }

    protected Duration getResponseTimeout(GatewayRoute route) {
        String responseTimeout = route.getMetadata().get(GatewayRoute.METADATA_RESPONSE_TIMEOUT_KEY);
        return responseTimeout == null || responseTimeout.isEmpty() ? null : Duration.parse(responseTimeout);
    }

    protected void cleanup(ServerRequest request, ServerResponse response) {
        Connection connection = (Connection) request.getAttribute(GatewayRoute.CLIENT_CONNECTION_ATTRIBUTE);
        if (connection != null) {
            connection.dispose();
        }
    }
}
