package com.kfyty.loveqq.framework.boot.mvc.server.netty.http;

import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.lang.Value;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import com.kfyty.loveqq.framework.core.proxy.factory.DynamicProxyFactory;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.multipart.DefaultMultipartFile;
import com.kfyty.loveqq.framework.web.core.multipart.MultipartFile;
import com.kfyty.loveqq.framework.web.mvc.reactor.exception.ReactiveServerException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.ByteBufMono;
import reactor.netty.http.server.HttpServerRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.kfyty.loveqq.framework.core.lang.ConstantConfig.IO_STREAM_READ_BUFFER_SIZE;
import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_INPUT_STREAM;
import static java.util.Collections.emptyList;

/**
 * 描述: netty 实现
 *
 * @author kfyty725
 * @date 2024/7/6 19:09
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class NettyServerRequest implements ServerRequest {
    /**
     * request
     */
    private final HttpServerRequest request;

    /**
     * uri
     */
    private final String uri;

    /**
     * attribute
     */
    private final Map<String, Object> attributes;

    /**
     * multipart
     */
    private final Map<String, MultipartFile> multipart;

    /**
     * query and form parameters
     */
    private final Map<String, String> parameters;

    /**
     * body is already read
     */
    private final AtomicBoolean isReadBody;

    /**
     * 非表单且非上传文件时有效
     */
    private final Value<InputStream> stream;

    /**
     * 请求体
     */
    private final Flux<ByteBuf> body;

    public NettyServerRequest(HttpServerRequest request) {
        this(request, resolveURI(request.uri()), request.receive(), new HashMap<>(), new HashMap<>(), CommonUtil.resolveURLParameters(request.uri()));
    }

    protected NettyServerRequest(HttpServerRequest request, String uri, Flux<ByteBuf> body, Map<String, Object> attributes, Map<String, MultipartFile> multipart, Map<String, String> parameters) {
        this.request = request;
        this.uri = uri;
        this.attributes = attributes;
        this.multipart = multipart;
        this.parameters = parameters;
        this.isReadBody = new AtomicBoolean(false);
        this.stream = new Value<>();
        this.body = body;
    }

    @Override
    public String getScheme() {
        return this.request.scheme();
    }

    @Override
    public String getHost() {
        return this.request.hostName();
    }

    @Override
    public Integer getServerPort() {
        return this.request.hostPort();
    }

    @Override
    public String getMethod() {
        return this.request.method().name();
    }

    @Override
    public String getRequestURL() {
        return this.request.scheme() + "://" + this.request.hostName() + ":" + this.request.hostPort() + this.getRequestURI();
    }

    @Override
    public String getRequestURI() {
        return this.uri;
    }

    @Override
    public String getContentType() {
        return this.getHeader(HttpHeaderNames.CONTENT_TYPE.toString());
    }

    @Override
    public InputStream getInputStream() {
        return this.stream.get();
    }

    @Override
    public MultipartFile getMultipart(String name) {
        return this.multipart.get(name);
    }

    @Override
    public Collection<MultipartFile> getMultipart() {
        return this.multipart.values();
    }

    @Override
    public String getParameter(String name) {
        return this.parameters.get(name);
    }

    @Override
    public Collection<String> getParameterNames() {
        return this.parameters.keySet();
    }

    @Override
    public Map<String, String> getParameterMap() {
        return this.parameters;
    }

    @Override
    public String getHeader(String name) {
        return this.request.requestHeaders().get(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return this.request.requestHeaders().getAll(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return this.request.requestHeaders().names();
    }

    @Override
    public HttpCookie getCookie(String name) {
        Set<Cookie> cookies = this.request.cookies().get(name);
        if (cookies == null || cookies.isEmpty()) {
            return null;
        }
        Cookie cookie = cookies.iterator().next();
        return new HttpCookie(cookie.name(), cookie.value());
    }

    @Override
    public HttpCookie[] getCookies() {
        return this.request.cookies().values().stream().flatMap(e -> e.stream().map(cookie -> new HttpCookie(cookie.name(), cookie.value()))).toArray(HttpCookie[]::new);
    }

    @Override
    public void setAttribute(String name, Object o) {
        this.attributes.put(name, o);
    }

    @Override
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    @Override
    public Map<String, Object> getAttributeMap() {
        return this.attributes;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return this.request.remoteAddress();
    }

    @Override
    public Locale getLocale() {
        String language = this.request.requestHeaders().get("Accept-Language");
        return language == null || language.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(language);
    }

    @Override
    public HttpServerRequest getRawRequest() {
        return this.request;
    }

    @Override
    public Flux<ByteBuf> getBody() {
        return this.body;
    }

    @Override
    public Mono<ByteBuf> getAggregateBody() {
        if (this.body instanceof ByteBufFlux) {
            return ((ByteBufFlux) this.body).aggregate();
        }
        return ByteBufFlux.fromInbound(this.body).aggregate();
    }

    public Mono<ServerRequest> receive() {
        if (isReadBody.get() || !isReadBody.compareAndSet(false, true)) {
            return Mono.just(this);
        }
        if (request.method() == HttpMethod.POST && (request.isFormUrlencoded() || request.isMultipart())) {
            return request.receiveForm()
                    .map(NettyServerRequest::buildForm)
                    .collectList()
                    .switchIfEmpty(Mono.just(emptyList()))
                    .flatMap(formData -> Mono.just(formData).doFinally(s -> formData.stream().map(Value::getValue).filter(e -> e instanceof MultipartFile).forEach(IOUtil::close)))
                    .doOnNext(formData -> {
                        for (Pair<String, Object> form : formData) {
                            if (form.getValue() instanceof MultipartFile) {
                                this.multipart.put(form.getKey(), (MultipartFile) form.getValue());
                            } else {
                                this.parameters.put(form.getKey(), form.getValue().toString());
                            }
                        }
                    })
                    .thenReturn(this);
        }
        return ((ByteBufMono) this.getAggregateBody())
                .asInputStream()
                .switchIfEmpty(Mono.just(EMPTY_INPUT_STREAM))
                .flatMap(body -> Mono.just(body).doFinally(s -> IOUtil.close(body)))
                .doOnNext(this.stream::set)
                .thenReturn(this);
    }

    @Override
    public ServerRequestBuilder mutate() {
        return new DefaultServerRequestBuilder();
    }

    public static String resolveURI(String uri) {
        int index1 = uri.indexOf('?');
        int index2 = uri.indexOf('#');
        int index = Math.min(index1 > -1 ? index1 : Integer.MAX_VALUE, index2 > -1 ? index2 : Integer.MAX_VALUE);
        if (index < Integer.MAX_VALUE) {
            uri = uri.substring(0, index);
        }
        return CommonUtil.formatURI(uri);
    }

    public static Pair<String, Object> buildForm(HttpData form) {
        if (!(form instanceof FileUpload)) {
            return new Pair<>(form.getName(), new String(getBytes(form)));
        }
        FileUpload upload = (FileUpload) form;
        Lazy<InputStream> lazyInputStream = Lazy.of(newInputStream(form));
        return new Pair<>(form.getName(), new DefaultMultipartFile(upload.getName(), upload.getFilename(), upload.getContentType(), true, upload.length(), lazyInputStream));
    }

    public static byte[] getBytes(HttpData data) {
        try {
            return data.get();
        } catch (IOException e) {
            throw new ReactiveServerException(e);
        }
    }

    public static File getFile(HttpData data) {
        try {
            return data.getFile();
        } catch (IOException e) {
            throw new ReactiveServerException(e);
        }
    }

    public static File renameTo(HttpData data, File dest) {
        try {
            if (!data.renameTo(dest)) {
                throw new ReactiveServerException("Rename failed.");
            }
            dest.deleteOnExit();
            return dest;
        } catch (IOException e) {
            throw new ReactiveServerException(e);
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
    public static Lazy.ThrowableSupplier<InputStream> newInputStream(HttpData upload) {
        if (upload.isInMemory()) {
            byte[] bytes = getBytes(upload);
            return () -> new ByteArrayInputStream(bytes);
        }
        File file = getFile(upload);
        File renamed = renameTo(upload, new File(file.getParent(), UUID.randomUUID().toString().replace('-', '_')));
        return () -> IOUtil.newInputStream(renamed);
    }

    /**
     * 默认实现
     */
    protected class DefaultServerRequestBuilder implements ServerRequestBuilder {
        protected String path;
        protected Flux<?> body;

        public DefaultServerRequestBuilder() {
            this.path = NettyServerRequest.this.uri;
            this.body = NettyServerRequest.this.body;
        }

        @Override
        public ServerRequestBuilder path(String path) {
            this.path = path;
            return this;
        }

        @Override
        public ServerRequestBuilder headers(String name, String... values) {
            return headers(true, name, values);
        }

        @Override
        public ServerRequestBuilder headers(boolean append, String name, String... values) {
            HttpHeaders headers = NettyServerRequest.this.request.requestHeaders();
            if (append) {
                headers.add(name, Arrays.asList(values));
            } else {
                headers.set(name, Arrays.asList(values));
            }
            return this;
        }

        @Override
        public ServerRequestBuilder body(String body) {
            return body(body.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public ServerRequestBuilder body(byte[] body) {
            return body(Flux.just(Unpooled.wrappedBuffer(body)));
        }

        @Override
        public ServerRequestBuilder body(InputStream body) {
            return body(Flux.create((Consumer<? super FluxSink<ByteBuf>>) sink -> {
                try (InputStream stream = body) {
                    int n = -1;
                    byte[] buffer = new byte[Math.max(IO_STREAM_READ_BUFFER_SIZE, stream.available())];
                    while ((n = stream.read(buffer)) != -1) {
                        sink.next(Unpooled.wrappedBuffer(buffer, 0, n));
                    }
                    sink.complete();
                } catch (IOException e) {
                    sink.error(e);
                }
            }));
        }

        @Override
        public ServerRequestBuilder body(Flux<ByteBuf> body) {
            // 适配表单及文件上传
            HttpServerRequest request = NettyServerRequest.this.request;
            if (request.method() == HttpMethod.POST && (request.isFormUrlencoded() || request.isMultipart())) {
                this.body = body.map(DefaultLastHttpContent::new);
                return this;
            }

            // 其他类型
            this.body = body;
            return this;
        }

        @Override
        public ServerRequest build() {
            return new NettyServerRequest(
                    this.createHttpServerRequestProxy(NettyServerRequest.this.request),
                    this.path,
                    (Flux) this.body,
                    NettyServerRequest.this.attributes,
                    NettyServerRequest.this.multipart,
                    NettyServerRequest.this.parameters
            );
        }

        /**
         * 创建请求代理，用于支持请求体修改
         * 仅当请求体修改时才创建代理
         * 仅当 {@link HttpServerRequest} 的实例是非 final 时才支持代理
         *
         * @return {@link HttpServerRequest} 代理
         */
        protected HttpServerRequest createHttpServerRequestProxy(HttpServerRequest target) {
            // 请求体未修改，无需代理
            if (this.body == NettyServerRequest.this.body) {
                return target;
            }

            // final 无法代理，警告
            if (Modifier.isFinal(target.getClass().getModifiers())) {
                log.warn("The HttpServerRequest instance doesn't support proxy yet, the mutate body will not be apply: {}", target);
                return target;
            }

            // 创建代理
            DynamicProxyFactory proxyFactory = DynamicProxyFactory.create(true).addInterceptorPoint(new HttpServerRequestProxy());
            for (Constructor<?> constructor : target.getClass().getDeclaredConstructors()) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 0) {
                    return proxyFactory.createProxy(target);
                }
                if (parameterTypes.length == 1 && parameterTypes[0].isInstance(target)) {
                    return proxyFactory.createProxy(target, parameterTypes, new Object[]{target});
                }
            }

            // 找不到合适的构造器，此时尝试使用 SunReflectionSupport 创建
            HttpServerRequest proxy = proxyFactory.createProxy(target);
            BeanUtil.copyProperties(target, proxy);
            return proxy;
        }

        private class HttpServerRequestProxy implements MethodInterceptorChainPoint {

            @Override
            public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
                final String methodName = methodProxy.getTargetMethod().getName();
                if (methodName.equals("receiveObject")) {
                    Flux<?> invoked = (Flux<?>) methodProxy.invoke();
                    return invoked.thenMany(DefaultServerRequestBuilder.this.body);
                }
                return methodProxy.getProxyMethod().invoke(methodProxy.getProxy(), methodProxy.getArguments());
            }
        }
    }
}
