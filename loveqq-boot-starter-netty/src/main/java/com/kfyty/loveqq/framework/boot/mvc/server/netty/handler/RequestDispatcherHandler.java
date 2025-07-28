package com.kfyty.loveqq.framework.boot.mvc.server.netty.handler;

import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.web.core.filter.Filter;
import com.kfyty.loveqq.framework.web.core.filter.reactor.DefaultFilterChain;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.mvc.netty.DispatcherHandler;
import com.kfyty.loveqq.framework.web.mvc.netty.http.NettyServerRequest;
import com.kfyty.loveqq.framework.web.mvc.netty.http.NettyServerResponse;
import com.kfyty.loveqq.framework.web.mvc.netty.ws.WebSocketHandler;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

/**
 * 描述: 请求分发处理器
 *
 * @author kfyty725
 * @date 2024/7/5 11:37
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class RequestDispatcherHandler implements BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> {
    /**
     * 路径匹配器
     */
    private final PatternMatcher patternMatcher;

    /**
     * 过滤器
     */
    private final List<Filter> filters;

    /**
     * 分发处理器
     */
    private final DispatcherHandler dispatcherHandler;

    /**
     * websocket 处理器
     */
    private final Map<String, WebSocketHandler> webSocketHandlerMap;

    @Override
    public Publisher<Void> apply(HttpServerRequest request, HttpServerResponse response) {
        // 资源处理器已处理
        if (response.hasSentHeaders()) {
            return Mono.empty();
        }

        // websocket
        if (request.requestHeaders().containsValue(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE, true)) {
            ServerRequest serverRequest = new NettyServerRequest(request);
            WebSocketHandler webSocketHandler = this.webSocketHandlerMap.get(serverRequest.getRequestURI());
            if (webSocketHandler == null) {
                return response.sendNotFound();
            }
            BiFunction<ServerRequest, ServerResponse, Publisher<Void>> requestProcessor = (req, res) -> response.sendWebsocket(this.upgradeWebSocketHandler(serverRequest, webSocketHandler));
            return new DefaultFilterChain(this.patternMatcher, this.filters, requestProcessor).doFilter(serverRequest, null);
        }

        // 处理请求
        return Mono.from(this.processRequest(request, response)).onErrorResume(new OnErrorResumeHandler(response));
    }

    protected BiFunction<? super WebsocketInbound, ? super WebsocketOutbound, ? extends Publisher<Void>> upgradeWebSocketHandler(ServerRequest request, WebSocketHandler webSocketHandler) {
        AtomicReference<Connection> reference = new AtomicReference<>();
        ((HttpServerRequest) request.getRawRequest()).withConnection(reference::set);
        return new UpgradeWebSocketHandler(request, reference, webSocketHandler);
    }

    protected Publisher<Void> processRequest(HttpServerRequest serverRequest, HttpServerResponse serverResponse) {
        // 构建通用请求/响应对象
        ServerRequest request = new NettyServerRequest(serverRequest);
        ServerResponse response = new NettyServerResponse(serverResponse);

        // 构建请求处理器
        BiFunction<ServerRequest, ServerResponse, Publisher<Void>> requestProcessor = (req, res) -> {
            if (serverResponse.hasSentHeaders()) {
                return Mono.empty();
            }
            if (serverRequest.method() == HttpMethod.OPTIONS) {
                return serverResponse.send();
            }
            return this.dispatcherHandler.service(req, res);
        };

        return new DefaultFilterChain(this.patternMatcher, this.filters, requestProcessor).doFilter(request, response);
    }
}
