package com.kfyty.loveqq.framework.web.mvc.netty.http;

import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.lang.Value;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.multipart.DefaultMultipartFile;
import com.kfyty.loveqq.framework.web.core.multipart.MultipartFile;
import com.kfyty.loveqq.framework.web.mvc.netty.exception.NettyServerException;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.server.HttpServerRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_INPUT_STREAM;
import static java.util.Collections.emptyList;

/**
 * 描述: netty 实现
 *
 * @author kfyty725
 * @date 2024/7/6 19:09
 * @email kfyty725@hotmail.com
 */
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
    private final Value<ByteBufFlux> body;

    public NettyServerRequest(HttpServerRequest request) {
        this.request = request;
        this.uri = this.resolveURI(request.uri());
        this.attributes = new HashMap<>();
        this.multipart = new HashMap<>();
        this.parameters = CommonUtil.resolveURLParameters(this.request.uri());
        this.isReadBody = new AtomicBoolean(false);
        this.stream = new Value<>();
        this.body = new Value<>(request.receive());
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
        return this.body.get();
    }

    @Override
    public Mono<ByteBuf> getAggregateBody() {
        return this.body.get().aggregate();
    }

    @Override
    public Mono<Void> mutateBody(Flux<ByteBuf> body) {
        return Mono.just(body)
                .flatMap(bodyElement -> {
                    if (isReadBody.get() && !isReadBody.compareAndSet(true, false)) {
                        return Mono.error(new IllegalStateException("Request body already read."));
                    }
                    this.multipart.clear();
                    this.parameters.clear();
                    this.parameters.putAll(CommonUtil.resolveURLParameters(this.request.uri()));
                    this.stream.set(null);
                    this.body.set(ByteBufFlux.fromInbound(bodyElement));
                    return Mono.empty();
                });
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
                            if (form.getValue() instanceof MultipartFile file) {
                                this.multipart.put(form.getKey(), file);
                            } else {
                                this.parameters.put(form.getKey(), form.getValue().toString());
                            }
                        }
                    })
                    .thenReturn(this);
        }
        return this.body.get()
                .aggregate()
                .asInputStream()
                .switchIfEmpty(Mono.just(EMPTY_INPUT_STREAM))
                .flatMap(body -> Mono.just(body).doFinally(s -> IOUtil.close(body)))
                .doOnNext(this.stream::set)
                .thenReturn(this);
    }

    protected String resolveURI(String uri) {
        int index1 = uri.indexOf('?');
        int index2 = uri.indexOf('#');
        int index = Math.min(index1 > -1 ? index1 : Integer.MAX_VALUE, index2 > -1 ? index2 : Integer.MAX_VALUE);
        if (index < Integer.MAX_VALUE) {
            uri = uri.substring(0, index);
        }
        return CommonUtil.formatURI(uri);
    }

    public static Pair<String, Object> buildForm(HttpData form) {
        if (!(form instanceof FileUpload upload)) {
            return new Pair<>(form.getName(), new String(getBytes(form)));
        }
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
