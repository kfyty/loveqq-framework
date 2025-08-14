package com.kfyty.loveqq.framework.boot.mvc.server.netty.http.filter;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.route.GatewayRoute;
import com.kfyty.loveqq.framework.web.core.route.gateway.GatewayFilterChain;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.ReferenceCounted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

import java.net.URI;
import java.util.function.BiFunction;

import static com.kfyty.loveqq.framework.web.core.route.gateway.LoadBalanceGatewayFilter.UPGRADE;
import static io.netty.handler.codec.http.websocketx.WebSocketCloseStatus.INTERNAL_SERVER_ERROR;

/**
 * 功能描述: 实际的 websocket 转发路由过滤器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
@RequiredArgsConstructor
@Order(Order.LOWEST_PRECEDENCE + 1)
@Component(GatewayRoute.DEFAULT_WEB_SOCKET_FORWARD_FILTER_NAME)
public class WebSocketForwardRouteGatewayFilter extends AbstractNettyRouteGatewayFilter {
    /**
     * 默认的 http client
     */
    private final HttpClient client;

    @Override
    public Mono<Void> doFilter(GatewayRoute route, ServerRequest request, ServerResponse response, GatewayFilterChain chain) {
        if (!UPGRADE.equalsIgnoreCase(request.getHeader("connection"))) {
            return chain.doFilter(request, response);
        }
        final URI routeURI = this.createRouteURI(route, request);
        final HttpServerResponse rawResponse = response.getRawResponse();
        return rawResponse.sendWebsocket(new ForwardWebSocketHandler(routeURI, request, response));
    }

    @RequiredArgsConstructor
    protected class ForwardWebSocketHandler implements BiFunction<WebsocketInbound, WebsocketOutbound, Publisher<Void>> {
        private final URI route;
        private final ServerRequest request;
        private final ServerResponse response;

        @Override
        public Publisher<Void> apply(WebsocketInbound inbound, WebsocketOutbound outbound) {
            return client.headers(headers -> {
                        for (String headerName : request.getHeaderNames()) {
                            headers.add(headerName, request.getHeaders(headerName));
                        }
                    })
                    .websocket()
                    .uri(route)
                    .handle(new ClientWebSocketHandler(inbound, outbound));
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    protected static class ClientWebSocketHandler implements BiFunction<WebsocketInbound, WebsocketOutbound, Publisher<Void>> {
        private final WebsocketInbound serverInbound;
        private final WebsocketOutbound serverOutbound;

        @Override
        public Publisher<Void> apply(WebsocketInbound clientInbound, WebsocketOutbound clientOutbound) {
            Flux<Void> serverPublisher = this.createForwardPublisher(serverInbound, clientOutbound);
            Flux<Void> clientPublisher = this.createForwardPublisher(clientInbound, serverOutbound);
            return Flux.zip(serverPublisher, clientPublisher).then();
        }

        /**
         * 创建转发发布者
         *
         * @param inbound  入栈
         * @param outbound 转发出栈
         * @return 发布者
         */
        protected Flux<Void> createForwardPublisher(WebsocketInbound inbound, WebsocketOutbound outbound) {
            Pair<WebSocketCloseStatus, Throwable> statusRef = new Pair<>(WebSocketCloseStatus.NORMAL_CLOSURE, new ResolvableException("Bye"));
            return inbound.receiveFrames()
                    .map(WebSocketFrame::retain)
                    .doOnDiscard(WebSocketFrame.class, ReferenceCounted::release)
                    .flatMap(frame -> outbound.sendObject(Mono.just(frame)))
                    .doOnError(ex -> statusRef.setKeyValue(INTERNAL_SERVER_ERROR, ex))
                    .onErrorComplete()
                    .doOnComplete(() -> outbound.sendClose(statusRef.getKey().code(), statusRef.getValue().getMessage()).subscribeOn(Schedulers.parallel()).subscribe());
        }
    }
}
