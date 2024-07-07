package com.kfyty.loveqq.framework.web.mvc.netty.http;

import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.multipart.MultipartFile;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.cookie.Cookie;
import reactor.netty.http.server.HttpServerRequest;

import java.io.InputStream;
import java.net.HttpCookie;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
     * attribute
     */
    private final Map<String, Object> attributes;

    /**
     * query and form parameters
     */
    private Map<String, String> parameters;

    /**
     * multipart
     */
    private Map<String, MultipartFile> multipart;

    /**
     * body
     */
    private InputStream body;

    public NettyServerRequest(HttpServerRequest request) {
        this.request = request;
        this.attributes = new ConcurrentHashMap<>();
    }

    public NettyServerRequest init(InputStream body, List<Pair<String, Object>> formData) {
        this.body = body;
        this.multipart = formData.stream().filter(e -> e.getValue() instanceof MultipartFile).collect(Collectors.toMap(Pair::getKey, v -> (MultipartFile) v.getValue()));
        this.parameters = formData.stream().filter(e -> !(e.getValue() instanceof MultipartFile)).collect(Collectors.toMap(Pair::getKey, v -> v.getValue().toString()));
        this.parameters.putAll(CommonUtil.resolveURLParameters(this.request.uri()));
        return this;
    }

    @Override
    public String getScheme() {
        return this.request.scheme();
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
        return this.request.scheme() + "://" + this.request.hostName() + ":" + this.request.hostPort() + this.request.fullPath();
    }

    @Override
    public String getRequestURI() {
        return this.request.fullPath();
    }

    @Override
    public String getContentType() {
        return this.getHeader(HttpHeaderNames.CONTENT_TYPE.toString());
    }

    @Override
    public InputStream getInputStream() {
        return this.body;
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
    public Object getRawRequest() {
        return this.request;
    }
}
