package com.kfyty.loveqq.framework.boot.mvc.server.netty.handler;

import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.web.core.filter.Filter;
import com.kfyty.loveqq.framework.web.core.filter.reactor.DefaultFilterChain;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.multipart.DefaultMultipartFile;
import com.kfyty.loveqq.framework.web.core.multipart.MultipartFile;
import com.kfyty.loveqq.framework.web.mvc.netty.DispatcherHandler;
import com.kfyty.loveqq.framework.web.mvc.netty.exception.NettyServerException;
import com.kfyty.loveqq.framework.web.mvc.netty.http.NettyServerRequest;
import com.kfyty.loveqq.framework.web.mvc.netty.http.NettyServerResponse;
import com.kfyty.loveqq.framework.web.mvc.netty.ws.WebSocketHandler;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_INPUT_STREAM;
import static com.kfyty.loveqq.framework.core.utils.IOUtil.close;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

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
            return Mono.from(this.processRequest(request, response, EMPTY_INPUT_STREAM, emptyList())).onErrorResume(new OnErrorResumeHandler(response));
        }

        // 接收数据后执行，否则拿不到数据，reactor-netty 限制必须为 POST
        if (request.method() == HttpMethod.POST && (request.isFormUrlencoded() || request.isMultipart())) {
            return request.receiveForm()
                    .map(RequestDispatcherHandler::buildForm)
                    .collectList()
                    .switchIfEmpty(Mono.just(emptyList()))
                    .flatMap(formData -> Mono.from(this.processRequest(request, response, EMPTY_INPUT_STREAM, formData)).doFinally(s -> formData.stream().filter(e -> e instanceof MultipartFile).forEach(IOUtil::close)))
                    .onErrorResume(new OnErrorResumeHandler(response));
        }
        return Mono.from(request.receive().asInputStream())
                .switchIfEmpty(Mono.just(EMPTY_INPUT_STREAM))
                .flatMap(body -> Mono.from(this.processRequest(request, response, body, emptyList())).doFinally(s -> close(body)))
                .onErrorResume(new OnErrorResumeHandler(response));
    }

    protected BiFunction<? super WebsocketInbound, ? super WebsocketOutbound, ? extends Publisher<Void>> upgradeWebSocketHandler(ServerRequest request, WebSocketHandler webSocketHandler) {
        AtomicReference<Connection> reference = new AtomicReference<>();
        ((HttpServerRequest) request.getRawRequest()).withConnection(reference::set);
        return new UpgradeWebSocketHandler(request, reference, webSocketHandler);
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

    public static Pair<String, Object> buildForm(HttpData form) {
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
