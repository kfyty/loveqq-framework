package com.kfyty.loveqq.framework.boot.mvc.server.netty;

import com.kfyty.loveqq.framework.boot.mvc.server.netty.autoconfig.NettyProperties;
import com.kfyty.loveqq.framework.boot.mvc.server.netty.socket.OioBasedLoopResources;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.support.AntPathMatcher;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.web.core.AbstractDispatcher;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.multipart.DefaultMultipartFile;
import com.kfyty.loveqq.framework.web.core.multipart.MultipartFile;
import com.kfyty.loveqq.framework.web.mvc.netty.DispatcherHandler;
import com.kfyty.loveqq.framework.web.mvc.netty.ServerWebServer;
import com.kfyty.loveqq.framework.web.mvc.netty.exception.NettyServerException;
import com.kfyty.loveqq.framework.web.core.filter.reactor.DefaultFilterChain;
import com.kfyty.loveqq.framework.web.core.filter.Filter;
import com.kfyty.loveqq.framework.web.mvc.netty.FilterRegistrationBean;
import com.kfyty.loveqq.framework.web.mvc.netty.http.NettyServerRequest;
import com.kfyty.loveqq.framework.web.mvc.netty.http.NettyServerResponse;
import com.kfyty.loveqq.framework.web.mvc.netty.ws.DefaultSession;
import com.kfyty.loveqq.framework.web.mvc.netty.ws.WebSocketHandler;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.util.ReferenceCounted;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.DisposableServer;
import reactor.netty.ReactorNetty;
import reactor.netty.http.HttpOperations;
import reactor.netty.http.HttpResources;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerState;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_INPUT_STREAM;
import static com.kfyty.loveqq.framework.core.utils.IOUtil.close;
import static com.kfyty.loveqq.framework.core.utils.IOUtil.read;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
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
     * 路径匹配器
     */
    @Setter
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
        System.setProperty(ReactorNetty.IO_SELECT_COUNT, this.config.getSelectThreads().toString());
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

        this.server = this.prepareStaticResource(this.server);
        this.server = this.prepareDispatcherHandler(this.server);
    }

    protected HttpServer prepareStaticResource(HttpServer server) {
        return server.childObserve(new ResourceConnectionObserver(this.patternMatcher, this.config));
    }

    protected BiFunction<? super WebsocketInbound, ? super WebsocketOutbound, ? extends Publisher<Void>> upgradeWebSocketHandler(ServerRequest request, WebSocketHandler webSocketHandler) {
        AtomicReference<Connection> reference = new AtomicReference<>();
        ((HttpServerRequest) request.getRawRequest()).withConnection(reference::set);
        return (inbound, outbound) -> {
            DefaultSession session = new DefaultSession(request, reference.get(), inbound, outbound);
            webSocketHandler.onOpen(session);
            inbound.aggregateFrames()
                    .receive()
                    .map(ByteBuf::retain)
                    .publishOn(Schedulers.parallel())
                    .doOnDiscard(ByteBuf.class, ReferenceCounted::release)
                    .subscribe(new CoreSubscriber<>() {

                        @Override
                        public void onSubscribe(Subscription s) {
                            s.request(Long.MAX_VALUE);
                        }

                        @Override
                        public void onNext(ByteBuf byteBuf) {
                            webSocketHandler.onMessage(session, byteBuf);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            webSocketHandler.onError(session, throwable);
                        }

                        @Override
                        public void onComplete() {
                            webSocketHandler.onClose(session);
                        }
                    });
            return outbound.neverComplete();
        };
    }

    protected HttpServer prepareDispatcherHandler(HttpServer server) {
        return server.handle((request, response) -> {
            // 资源处理器已处理
            if (response.hasSentHeaders()) {
                return Mono.empty();
            }

            // websocket
            if (request.requestHeaders().containsValue(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE, true)) {
                ServerRequest serverRequest = new NettyServerRequest(request).init(EMPTY_INPUT_STREAM, Collections.emptyList());
                WebSocketHandler webSocketHandler = this.webSocketHandlerMap.get(serverRequest.getRequestURI());
                if (webSocketHandler == null) {
                    return response.sendNotFound();
                }
                Supplier<Publisher<Void>> requestProcessorSupplier = () -> response.sendWebsocket(this.upgradeWebSocketHandler(serverRequest, webSocketHandler));
                return new DefaultFilterChain(this.patternMatcher, unmodifiableList(this.filters), requestProcessorSupplier).doFilter(serverRequest, null);
            }

            // 预检请求
            if (request.method() == HttpMethod.OPTIONS) {
                return Mono.from(this.processRequest(request, response, EMPTY_INPUT_STREAM, emptyList())).onErrorResume(new OnErrorResume(response));
            }

            // 接收数据后执行，否则拿不到数据，reactor-netty 限制必须为 POST
            if (request.method() == HttpMethod.POST && (request.isFormUrlencoded() || request.isMultipart())) {
                return request.receiveForm()
                        .map(this::buildForm)
                        .collectList()
                        .switchIfEmpty(Mono.just(emptyList()))
                        .flatMap(formData -> Mono.from(this.processRequest(request, response, EMPTY_INPUT_STREAM, formData)).doFinally(s -> formData.stream().filter(e -> e instanceof MultipartFile).forEach(IOUtil::close)))
                        .onErrorResume(new OnErrorResume(response));
            }
            return Mono.from(request.receive().asInputStream())
                    .switchIfEmpty(Mono.just(EMPTY_INPUT_STREAM))
                    .flatMap(body -> Mono.from(this.processRequest(request, response, body, emptyList())).doFinally(s -> close(body)))
                    .onErrorResume(new OnErrorResume(response));
        });
    }

    protected Publisher<Void> processRequest(HttpServerRequest serverRequest, HttpServerResponse serverResponse, InputStream body, List<Pair<String, Object>> formData) {
        // 构建通用请求/响应对象
        ServerRequest request = new NettyServerRequest(serverRequest).init(body, formData);
        ServerResponse response = new NettyServerResponse(serverResponse);

        // 构建请求处理器生产者
        Supplier<Publisher<Void>> requestProcessorSupplier = () -> {
            if (serverResponse.hasSentHeaders()) {
                return Mono.empty();
            }
            if (serverRequest.method() == HttpMethod.OPTIONS) {
                return serverResponse.send();
            }
            return this.dispatcherHandler.service(request, response);
        };

        return new DefaultFilterChain(this.patternMatcher, unmodifiableList(this.filters), requestProcessorSupplier).doFilter(request, response);
    }

    @Slf4j
    @RequiredArgsConstructor
    protected static class OnErrorResume implements Function<Throwable, Mono<Void>> {
        private final HttpServerResponse response;

        @Override
        public Mono<Void> apply(Throwable throwable) {
            log.error("Netty server request error.", throwable);
            if (response.hasSentHeaders()) {
                return response.then();
            }
            return response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR).send();
        }
    }

    @RequiredArgsConstructor
    protected static class ResourceConnectionObserver implements ConnectionObserver {
        /**
         * 路径匹配器
         */
        private final PatternMatcher patternMatcher;

        /**
         * 服务器属性配置
         */
        private final NettyProperties config;

        @Override
        public void onStateChange(Connection connection, State newState) {
            if (newState != HttpServerState.REQUEST_RECEIVED) {
                return;
            }
            try {
                HttpOperations<?, ?> operations = (HttpOperations<?, ?>) connection;
                HttpServerRequest request = (HttpServerRequest) connection;
                HttpServerResponse response = (HttpServerResponse) connection;

                // 获取请求资源路径
                String uri = request.fullPath();

                // 匹配项目资源
                for (String pattern : this.config.getStaticPattern()) {
                    if (this.patternMatcher.matches(pattern, uri)) {
                        URL resolved = this.config.getResourceResolver().resolve(uri);
                        if (resolved != null) {
                            this.sendResource(request, response, operations, resolved);
                            return;
                        }
                    }
                }

                // 匹配本地磁盘路径
                for (Pair<String, String> resource : this.config.getResources()) {
                    String pattern = resource.getKey() + "/**";
                    if (this.patternMatcher.matches(pattern, uri)) {
                        URL resolved = this.config.getResourceResolver().resolveNative(uri, resource);
                        if (resolved != null) {
                            this.sendResource(request, response, operations, resolved);
                            return;
                        }
                    }
                }
            } catch (Throwable e) {
                log.error(format(connection.channel(), ""), e);
                connection.channel().close();
            }
        }

        protected void sendResource(HttpServerRequest request, HttpServerResponse response, HttpOperations<?, ?> operations, URL url) {
            String contentType = URLConnection.getFileNameMap().getContentTypeFor(url.getFile());
            if (contentType != null) {
                if (!contentType.contains("charset")) {
                    contentType += ";charset=utf-8";
                }
                response.header(HttpHeaderNames.CONTENT_TYPE, contentType);
            }
            response.sendByteArray(Mono.fromSupplier(() -> read(IOUtil.newInputStream(url)))).subscribe(operations.disposeSubscriber());
        }
    }

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
            while (isStart()) {
                CommonUtil.sleep(200);
            }
        }
    }

    public Pair<String, Object> buildForm(HttpData form) {
        if (!(form instanceof FileUpload)) {
            return new Pair<>(form.getName(), new String(getBytes(form)));
        }
        FileUpload upload = (FileUpload) form;
        Lazy<InputStream> lazyInputStream = new Lazy<>(newInputStream(form));
        return new Pair<>(form.getName(), new DefaultMultipartFile(upload.getName(), upload.getFilename(), upload.getContentType(), true, upload.length(), lazyInputStream));
    }

    public static byte[] getBytes(HttpData data) {
        try {
            return data.get();
        } catch (IOException e) {
            throw new NettyServerException(e);
        }
    }

    public static File getFile(HttpData data) {
        try {
            return data.getFile();
        } catch (IOException e) {
            throw new NettyServerException(e);
        }
    }

    public static File renameTo(HttpData data, File dest) {
        try {
            if (!data.renameTo(dest)) {
                throw new NettyServerException("Rename failed.");
            }
            dest.deleteOnExit();
            return dest;
        } catch (IOException e) {
            throw new NettyServerException(e);
        }
    }

    /**
     * 构建输入流提供者
     * <p>
     * 如果在内存中则先复制到字节数组，否则该订阅流程结束后会被释放，后续获取不到
     * <p>
     * 如果是文件则重命名，否则也会被释放，后续获取不到
     *
     * @param upload 上传数据
     * @return 输入流提供者
     */
    public static Supplier<InputStream> newInputStream(HttpData upload) {
        if (upload.isInMemory()) {
            byte[] bytes = getBytes(upload);
            return () -> new ByteArrayInputStream(bytes);
        }
        File file = getFile(upload);
        File renamed = renameTo(upload, new File(file.getParent(), UUID.randomUUID().toString().replace('-', '_')));
        return () -> IOUtil.newInputStream(renamed);
    }
}
