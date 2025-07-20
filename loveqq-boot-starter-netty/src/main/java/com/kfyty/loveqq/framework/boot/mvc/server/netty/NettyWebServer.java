package com.kfyty.loveqq.framework.boot.mvc.server.netty;

import com.kfyty.loveqq.framework.boot.mvc.server.netty.autoconfig.NettyProperties;
import com.kfyty.loveqq.framework.boot.mvc.server.netty.handler.RequestDispatcherHandler;
import com.kfyty.loveqq.framework.boot.mvc.server.netty.handler.ResourcesHandler;
import com.kfyty.loveqq.framework.boot.mvc.server.netty.socket.OioBasedLoopResources;
import com.kfyty.loveqq.framework.core.support.AntPathMatcher;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.AbstractDispatcher;
import com.kfyty.loveqq.framework.web.core.filter.Filter;
import com.kfyty.loveqq.framework.web.mvc.netty.DispatcherHandler;
import com.kfyty.loveqq.framework.web.mvc.netty.FilterRegistrationBean;
import com.kfyty.loveqq.framework.web.mvc.netty.ServerWebServer;
import com.kfyty.loveqq.framework.web.mvc.netty.ws.WebSocketHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.DisposableServer;
import reactor.netty.ReactorNetty;
import reactor.netty.http.HttpResources;
import reactor.netty.http.server.HttpServer;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
     * 路径匹配器
     */
    @Setter
    @Getter
    private PatternMatcher patternMatcher;

    /**
     * 服务器配置属性
     */
    @Setter
    @Getter
    private NettyProperties config;

    /**
     * 分发处理器
     */
    @Setter
    @Getter
    private DispatcherHandler dispatcherHandler;

    /**
     * 过滤器
     */
    @Setter
    private List<Filter> filters;

    /**
     * websocket 处理器
     */
    @Setter
    private Map<String, WebSocketHandler> webSocketHandlerMap;

    public NettyWebServer() {
        this(new NettyProperties());
    }

    public NettyWebServer(NettyProperties config) {
        this(config, new DispatcherHandler(), new ConcurrentHashMap<>(4));
    }

    public NettyWebServer(NettyProperties config, DispatcherHandler dispatcherHandler, Map<String, WebSocketHandler> webSocketHandlerMap) {
        this.config = config;
        this.filters = config.getWebFilters().stream().map(FilterRegistrationBean::getFilter).collect(Collectors.toList());
        this.dispatcherHandler = dispatcherHandler;
        this.webSocketHandlerMap = webSocketHandlerMap;
        this.patternMatcher = new AntPathMatcher();
        this.configNettyServer();
    }

    public void addWebSocketHandler(WebSocketHandler webSocketHandler) {
        this.webSocketHandlerMap.putIfAbsent(webSocketHandler.getEndPoint(), webSocketHandler);
    }

    @Override
    public void start() {
        if (!this.started) {
            this.started = true;
            this.disposableServer = this.server.bindNow();
            new DaemonServerTask().start();
            log.info("Netty started on port({})", this.getPort());
        }
    }

    @Override
    public void stop() {
        if (this.started) {
            this.started = false;
            this.disposableServer.disposeNow();                                                                         // shouldn't be null, otherwise has unknown error
            HttpResources.disposeLoopsAndConnections();                                                                 // actual close server resources
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

    @Override
    public AbstractDispatcher<?> getDispatcher() {
        return this.dispatcherHandler;
    }

    protected void configNettyServer() {
        // 先设置属性值，否则会因为静态加载而无效
        // 设置最大 select 线程数
        System.setProperty(ReactorNetty.IO_SELECT_COUNT, this.config.getSelectThreads().toString());

        // 设置转发是否严格验证 DefaultHttpForwardedHeaderHandler#FORWARDED_HEADER_VALIDATION
        System.setProperty("reactor.netty.http.server.forwarded.strictValidation", this.config.getForwardedStrictValidation().toString());

        // 设置最大工作线程数
        if (this.config.getMaxThreads() != null) {
            System.setProperty(ReactorNetty.IO_WORKER_COUNT, this.config.getMaxThreads().toString());
        }

        // 再配置服务器
        this.server = HttpServer.create()
                .port(this.getPort())
                .compress(this.config.getCompress())
                .forwarded(this.config.getForwarded())
                .accessLog(this.config.getAccessLog());
        if (this.config.isVirtualThread() && CommonUtil.VIRTUAL_THREAD_SUPPORTED) {
            this.server = this.server.runOn(HttpResources.set(new OioBasedLoopResources()));
        }
        if (this.config.getCompress() && this.config.getMinCompressionSize() != null) {
            this.server = this.server.compress(this.config.getMinCompressionSize());
        }
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
        if (this.config.getProtocol() != null) {
            this.server = this.server.protocol(this.config.getProtocol());
        }
        if (this.config.getSslProvider() != null) {
            this.server = this.server.secure(this.config.getSslProvider(), this.config.getRedirectHttpToHttps());
        }
        if (this.config.getLocation() != null || this.config.getMaxFileSize() != null || this.config.getMaxInMemorySize() != null) {
            this.server = this.server.httpFormDecoder(e -> {
                if (this.config.getLocation() != null) {
                    e.baseDirectory(Paths.get(this.config.getLocation()));
                }
                if (this.config.getMaxFileSize() != null) {
                    e.maxSize(this.config.getMaxFileSize());
                }
                if (this.config.getMaxInMemorySize() != null) {
                    e.maxInMemorySize(this.config.getMaxInMemorySize());
                }
            });
        }
        if (this.config.getServerConfigure() != null) {
            this.config.getServerConfigure().accept(this.server);
        }

        this.server = this.prepareResourceHandler(this.server);
        this.server = this.prepareDispatcherHandler(this.server);
    }

    protected HttpServer prepareResourceHandler(HttpServer server) {
        return server.childObserve(new ResourcesHandler(this.config, this.patternMatcher, this.filters));
    }

    protected HttpServer prepareDispatcherHandler(HttpServer server) {
        return server.handle(new RequestDispatcherHandler(this.patternMatcher, this.filters, this.dispatcherHandler, this.webSocketHandlerMap));
    }

    /**
     * 守护线程任务
     */
    protected class DaemonServerTask implements Runnable {
        /**
         * 守护线程是否已启动
         */
        private volatile boolean started;

        public void start() {
            Thread daemon = new Thread(this);
            daemon.setDaemon(false);
            daemon.start();

            // wait daemon start
            while (!started) ;
        }

        @Override
        public void run() {
            this.started = true;
            while (NettyWebServer.this.isStart()) {
                CommonUtil.sleep(200);
            }
        }
    }
}
