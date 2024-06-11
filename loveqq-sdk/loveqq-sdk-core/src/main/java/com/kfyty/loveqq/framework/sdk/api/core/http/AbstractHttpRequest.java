package com.kfyty.loveqq.framework.sdk.api.core.http;

import com.kfyty.loveqq.framework.core.utils.BeanUtil;

import java.net.HttpCookie;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.processPlaceholder;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * 描述: 配置 http 请求基础实现
 *
 * @author kfyty725
 * @date 2021/11/15 9:01
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractHttpRequest<T extends HttpRequest<T>> implements HttpRequest<T> {
    /**
     * 连接超时时间，毫秒
     */
    protected Integer connectTimeout;

    /**
     * 请求超时时间，毫秒
     */
    protected Integer readTimeout;

    /**
     * payload
     */
    protected byte[] payload;

    /**
     * 请求头
     */
    protected Map<String, String> headers;

    /**
     * 请求参数
     */
    protected Map<String, Object> formData;

    /**
     * URL 查询参数，执行请求时会编码并拼接到 URL 之后
     */
    protected Map<String, String> query;

    /**
     * URL 路径参数
     */
    protected Map<String, String> path;

    /**
     * cookie
     */
    protected List<HttpCookie> cookies;

    /**
     * {@link HttpCookie#version}
     */
    protected int cookieVersion = 0;

    /**
     * 代理
     */
    protected Proxy proxy;

    /**
     * 代理选择器
     */
    protected ProxySelector proxySelector;

    @SuppressWarnings("unchecked")
    public T setConnectTimeout(int connectTimeout) {
        if (connectTimeout > 0) {
            this.connectTimeout = connectTimeout;
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setReadTimeout(int readTimeout) {
        if (readTimeout > 0) {
            this.readTimeout = readTimeout;
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setPayload(byte[] payload) {
        this.payload = payload;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setCookieVersion(int cookieVersion) {
        this.cookieVersion = cookieVersion;
        return (T) this;
    }

    @Override
    public byte[] payload() {
        return this.payload;
    }

    public T addHeader(String key, String value) {
        return this.addParameter(key, value, this.headers, () -> this.headers = new HashMap<>());
    }

    public T addFormData(String key, Object value) {
        return this.addParameter(key, value, this.formData, () -> this.formData = new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    public T addFormData(Object data) {
        BeanUtil.copyProperties(requireNonNull(data)).forEach(this::addFormData);
        return (T) this;
    }

    public T addQuery(String key, Object value) {
        return this.addParameter(key, value, this.query, () -> this.query = new LinkedHashMap<>());
    }

    public T addPath(String key, Object value) {
        return this.addParameter(key, value, this.path, () -> this.path = new LinkedHashMap<>());
    }

    public T addCookie(String key, String value) {
        return this.addCookie(new HttpCookie(key, value));
    }

    @SuppressWarnings("unchecked")
    public T addCookie(HttpCookie cookie) {
        if (this.cookies == null) {
            this.cookies = new ArrayList<>();
        }
        requireNonNull(cookie).setVersion(this.cookieVersion);
        this.cookies.add(cookie);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setProxy(Proxy proxy) {
        if (proxy != null) {
            this.proxy = proxy;
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setProxy(ProxySelector proxySelector) {
        if (proxySelector != null) {
            this.proxySelector = proxySelector;
        }
        return (T) this;
    }

    @Override
    public int connectTimeout() {
        return ofNullable(this.connectTimeout).orElseGet(HttpRequest.super::connectTimeout);
    }

    @Override
    public int readTimeout() {
        return ofNullable(this.readTimeout).orElseGet(HttpRequest.super::readTimeout);
    }

    @Override
    public Map<String, String> headers() {
        return ofNullable(this.headers).orElseGet(HttpRequest.super::headers);
    }

    @Override
    public Map<String, Object> formData() {
        return ofNullable(this.formData).orElseGet(HttpRequest.super::formData);
    }

    @Override
    public Map<String, String> queryParameters() {
        return ofNullable(this.query).orElseGet(HttpRequest.super::queryParameters);
    }

    @Override
    public List<HttpCookie> cookies() {
        return ofNullable(this.cookies).orElseGet(HttpRequest.super::cookies);
    }

    @Override
    public Proxy proxy() {
        return ofNullable(this.proxy).orElseGet(HttpRequest.super::proxy);
    }

    @Override
    public String requestURL() {
        String url = processPlaceholder(this.requestPath(), ofNullable(this.path).orElseGet(Collections::emptyMap));
        boolean hasParameter = url.contains("?");
        String parameters = this.queryParameters().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
        return hasParameter ? url + '&' + parameters : url + '?' + parameters;
    }

    @SuppressWarnings("unchecked")
    protected <V> T addParameter(String key, Object value, Map<String, V> container, Supplier<Map<String, V>> initProvider) {
        if (container == null) {
            container = initProvider.get();
        }
        container.put(key, (V) value);
        return (T) this;
    }
}
