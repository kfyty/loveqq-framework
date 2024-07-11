package com.kfyty.loveqq.framework.boot.mvc.server.netty;

import com.kfyty.loveqq.framework.boot.mvc.server.netty.autoconfig.NettyProperties;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.support.AntPathMatcher;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.multipart.DefaultMultipartFile;
import com.kfyty.loveqq.framework.web.core.multipart.MultipartFile;
import com.kfyty.loveqq.framework.web.mvc.netty.DispatcherHandler;
import com.kfyty.loveqq.framework.web.mvc.netty.ServerWebServer;
import com.kfyty.loveqq.framework.web.mvc.netty.exception.NettyServerException;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.DefaultFilterChain;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.Filter;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.FilterRegistrationBean;
import com.kfyty.loveqq.framework.web.mvc.netty.http.NettyServerRequest;
import com.kfyty.loveqq.framework.web.mvc.netty.http.NettyServerResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.DisposableServer;
import reactor.netty.http.HttpOperations;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerState;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.UUID;
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
    private NettyProperties config;

    /**
     * 过滤器
     */
    @Setter
    private List<Filter> filters;

    /**
     * 分发处理器
     */
    @Setter
    private DispatcherHandler dispatcherHandler;

    public NettyWebServer() {
        this(new NettyProperties());
    }

    public NettyWebServer(NettyProperties config) {
        this(config, null);
    }

    public NettyWebServer(NettyProperties config, DispatcherHandler dispatcherHandler) {
        this.config = config;
        this.filters = config.getWebFilters().stream().map(FilterRegistrationBean::getFilter).collect(Collectors.toList());
        this.dispatcherHandler = dispatcherHandler;
        this.patternMatcher = new AntPathMatcher();
        this.configNettyServer();
    }

    @Override
    public void start() {
        this.disposableServer = this.server.bindNow();
        this.started = true;
        new Thread(new DaemonServerTask()).start();
        log.info("Netty started on port({})", this.getPort());
    }

    @Override
    public void stop() {
        if (this.started) {
            this.disposableServer.disposeNow();
            this.started = false;
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

        this.server = this.prepareStaticResource(this.server);
        this.server = this.prepareDispatcherHandler(this.server);
    }

    protected HttpServer prepareStaticResource(HttpServer server) {
        return server.childObserve(new ResourceConnectionObserver(this.patternMatcher, this.config));
    }

    protected HttpServer prepareDispatcherHandler(HttpServer server) {
        return server.handle((request, response) -> {
            // 资源处理器已处理
            if (response.hasSentHeaders()) {
                return Mono.empty();
            }

            // 接收数据后执行，否则拿不到数据
            if (request.isFormUrlencoded() || request.isMultipart()) {
                return request.receiveForm()
                        .map(this::buildForm)
                        .collectList()
                        .switchIfEmpty(Mono.just(emptyList()))
                        .flatMap(formData -> Mono.from(this.processRequest(request, response, EMPTY_INPUT_STREAM, formData)).doFinally(s -> formData.stream().filter(e -> e instanceof MultipartFile).forEach(IOUtil::close)))
                        .onErrorResume(ex -> response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR).send());
            }
            return Mono.from(request.receive().asInputStream())
                    .switchIfEmpty(Mono.just(EMPTY_INPUT_STREAM))
                    .flatMap(body -> Mono.from(this.processRequest(request, response, body, emptyList())).doFinally(s -> close(body)))
                    .onErrorResume(ex -> response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR).send());
        });
    }

    protected Publisher<Void> processRequest(HttpServerRequest serverRequest, HttpServerResponse serverResponse, InputStream body, List<Pair<String, Object>> formData) {
        // 构建通用请求/响应对象
        ServerRequest request = new NettyServerRequest(serverRequest).init(body, formData);
        ServerResponse response = new NettyServerResponse(serverResponse);

        // 构建请求处理器生产者
        Supplier<Publisher<Void>> requestProcessorSupplier = () -> this.dispatcherHandler.service(request, response);

        return new DefaultFilterChain(this.patternMatcher, unmodifiableList(this.filters), requestProcessorSupplier).doFilter(request, response);
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

        @Override
        public void run() {
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
        return new Pair<>(form.getName(), new DefaultMultipartFile(upload.getName(), upload.getFilename(), upload.getContentType(), true, upload.getMaxSize(), lazyInputStream));
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
