package com.kfyty.sdk.api.core.decorate;

import com.kfyty.sdk.api.core.AbstractApi;
import com.kfyty.sdk.api.core.Api;
import com.kfyty.sdk.api.core.ApiResponse;
import com.kfyty.sdk.api.core.config.ApiConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.net.HttpCookie;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 描述: api 装饰器
 *
 * @author kun.zhang
 * @date 2021/11/15 09:25
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class ApiDecorate<T extends Api<T, R>, R extends ApiResponse> extends AbstractApi<T, R> {
    /**
     * 装饰的 api
     */
    protected AbstractApi<T, R> decorate;

    public ApiDecorate(AbstractApi<T, R> decorate) {
        this.decorate = Objects.requireNonNull(decorate);
    }

    @Override
    public String requestURL() {
        return this.decorate.requestURL();
    }

    @Override
    public String contentType() {
        return this.decorate.contentType();
    }

    @Override
    public String method() {
        return this.decorate.method();
    }

    @Override
    public int connectTimeout() {
        return this.decorate.connectTimeout();
    }

    @Override
    public int readTimeout() {
        return this.decorate.readTimeout();
    }

    @Override
    public Map<String, String> headers() {
        return this.decorate.headers();
    }

    @Override
    public Map<String, Object> formData() {
        return this.decorate.formData();
    }

    @Override
    public Map<String, String> queryParameters() {
        return this.decorate.queryParameters();
    }

    @Override
    public List<HttpCookie> cookies() {
        return this.decorate.cookies();
    }

    @Override
    public Proxy proxy() {
        return this.decorate.proxy();
    }

    @Override
    public String postProcessorURL() {
        return this.decorate.postProcessorURL();
    }

    @Override
    public T setConnectTimeout(int connectTimeout) {
        return this.decorate.setConnectTimeout(connectTimeout);
    }

    @Override
    public T setReadTimeout(int readTimeout) {
        return this.decorate.setReadTimeout(readTimeout);
    }

    @Override
    public T addHeader(String key, String value) {
        return this.decorate.addHeader(key, value);
    }

    @Override
    public T addFormData(String key, Object value) {
        return this.decorate.addFormData(key, value);
    }

    @Override
    public T addFormData(Object data) {
        return this.decorate.addFormData(data);
    }

    @Override
    public T addQuery(String key, Object value) {
        return this.decorate.addQuery(key, value);
    }

    @Override
    public T addCookie(String key, String value) {
        return this.decorate.addCookie(key, value);
    }

    @Override
    public T addCookie(HttpCookie cookie) {
        return this.decorate.addCookie(cookie);
    }

    @Override
    public T applyProxy(Proxy proxy) {
        return this.decorate.applyProxy(proxy);
    }

    @Override
    public String getBaseUrl() {
        return this.decorate.getBaseUrl();
    }

    @Override
    public ApiConfiguration getConfiguration() {
        return this.decorate.getConfiguration();
    }

    @Override
    public AbstractApi<T, R> setConfiguration(ApiConfiguration configuration) {
        return this.decorate.setConfiguration(configuration);
    }

    @Override
    public ApiConfiguration cloneConfig() {
        return this.decorate.cloneConfig();
    }

    @Override
    public Class<R> resolveResponseGeneric() {
        return this.decorate.resolveResponseGeneric();
    }

    @Override
    public String processURIVariable(String uri) {
        return this.decorate.processURIVariable(uri);
    }

    @Override
    public String getQueryURL() {
        return this.decorate.getQueryURL();
    }

    @Override
    public String getEncodeQueryURL() {
        return this.decorate.getEncodeQueryURL();
    }

    @Override
    public void preProcessor() {
        this.decorate.preProcessor();
    }

    @Override
    public void postProcessor(R response) {
        this.decorate.postProcessor(response);
    }
}
