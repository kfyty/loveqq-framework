package com.kfyty.loveqq.framework.web.mvc.netty;

import com.kfyty.loveqq.framework.web.mvc.netty.autoconfig.NettyServerProperties;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.DefaultFilterChain;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.Filter;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.FilterRegistrationBean;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerState;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

import static reactor.netty.ReactorNetty.format;

/**
 * 描述: reactor-netty server
 *
 * @author kfyty725
 * @date 2024/7/5 11:37
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class NettyWebServer implements ServerWebServer {
    /**
     * 是否已启动
     */
    private volatile boolean started;

    /**
     * 服务器
     */
    private HttpServer server;

    /**
     * {@link DisposableServer}
     */
    private DisposableServer disposableServer;

    /**
     * 服务器配置属性
     */
    @Setter
    private NettyServerProperties config;

    /**
     * 分发处理器
     */
    @Setter
    private DispatcherHandler dispatcherHandler;

    public NettyWebServer() {
        this(new NettyServerProperties());
    }

    public NettyWebServer(NettyServerProperties config) {
        this(config, null);
    }

    public NettyWebServer(NettyServerProperties config, DispatcherHandler dispatcherHandler) {
        this.config = config;
        this.dispatcherHandler = dispatcherHandler;
        this.configNettyServer();
    }

    @Override
    public void start() {
        this.disposableServer = this.server.bindNow();
        this.started = true;

    }

    @Override
    public void stop() {
        if (this.started) {
            this.started = false;
            this.disposableServer.disposeNow();
        }
    }

    @Override
    public boolean isStart() {
        return this.started;
    }

    @Override
    public int getPort() {
        return this.config.getPort();
    }

    protected void configNettyServer() {
        this.server = HttpServer.create()
                .port(this.getPort())
                .compress(this.config.getCompress())
                .forwarded(this.config.getForwarded())
                .accessLog(this.config.getAccessLog());
        if (this.config.getIdleTimeout() != null) {
            this.server = this.server.idleTimeout(this.config.getIdleTimeout());
        }
        if (this.config.getMaxKeepAliveRequests() != null) {
            this.server = this.server.maxKeepAliveRequests(this.config.getMaxKeepAliveRequests());
        }
        if (this.config.getReadTimeout() != null) {
            this.server = this.server.readTimeout(this.config.getReadTimeout());
        }
        if (this.config.getRequestTimeout() != null) {
            this.server = this.server.requestTimeout(this.config.getRequestTimeout());
        }
        if (this.config.getServerConfigure() != null) {
            this.config.getServerConfigure().accept(this.server);
        }

        this.server = this.prepareServerFilter(this.server);
        this.server = this.prepareDispatcherHandler(this.server);
    }

    protected HttpServer prepareServerFilter(HttpServer server) {
        List<Filter> filters = config.getWebFilters().stream().map(FilterRegistrationBean::getFilter).collect(Collectors.toList());
        return server.childObserve(new FilterConnectionObserver(Collections.unmodifiableList(filters)));
    }

    @SuppressWarnings("unchecked")
    protected HttpServer prepareDispatcherHandler(HttpServer server) {
        return server.handle((request, response) -> {
            Object retValue = this.dispatcherHandler.service(request, response);
            if (retValue == null) {
                return response.send();
            }
            if (retValue instanceof Flow.Publisher<?>) {
                return FlowAdapters.toPublisher((Flow.Publisher<Void>) retValue);
            }
            if (retValue instanceof Publisher<?>) {
                return (Publisher<Void>) retValue;
            }
            if (retValue instanceof CharSequence) {
                return response.sendString(Mono.just(retValue.toString()));
            }
            if (retValue instanceof byte[]) {
                return response.sendByteArray(Mono.just((byte[]) retValue));
            }
            return response.sendObject(Mono.just(response));
        });
    }

    @RequiredArgsConstructor
    protected static class FilterConnectionObserver implements ConnectionObserver {
        private final List<Filter> filters;

        @Override
        public void onStateChange(Connection connection, State newState) {
            if (newState != HttpServerState.REQUEST_RECEIVED) {
                return;
            }
            try {
                HttpServerRequest request = (HttpServerRequest) connection;
                HttpServerResponse response = (HttpServerResponse) connection;
                new DefaultFilterChain(this.filters).doFilter(request, response);
            } catch (Throwable e) {
                log.error(format(connection.channel(), ""), e);
                connection.channel().close();
            }
        }
    }

    protected class DaemonServerTask implements Runnable {

        @Override
        public void run() {

        }
    }
}
