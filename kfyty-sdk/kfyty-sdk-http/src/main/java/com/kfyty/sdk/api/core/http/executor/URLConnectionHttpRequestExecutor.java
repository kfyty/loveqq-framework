package com.kfyty.sdk.api.core.http.executor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.InputStreamResource;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import cn.hutool.http.cookie.GlobalCookieManager;
import com.kfyty.core.support.io.FilePart;
import com.kfyty.core.utils.JsonUtil;
import com.kfyty.sdk.api.core.constant.ApiConstants;
import com.kfyty.sdk.api.core.exception.ApiException;
import com.kfyty.sdk.api.core.http.HttpRequest;
import com.kfyty.sdk.api.core.http.HttpRequestExecutor;
import lombok.extern.slf4j.Slf4j;

import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;

/**
 * 描述: 基于 URL Connection 的执行器
 *
 * @author kfyty725
 * @date 2021/11/11 18:12
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class URLConnectionHttpRequestExecutor implements HttpRequestExecutor {

    @Override
    public URLConnectionHttpResponse wrapResponse(Object response) {
        return new URLConnectionHttpResponse((HttpResponse) response);
    }

    @Override
    public com.kfyty.sdk.api.core.http.HttpResponse exchange(HttpRequest<?> api, boolean validStatusCode) {
        long start = System.currentTimeMillis();
        URLConnectionHttpResponse response = this.wrapResponse(this.buildRequest(api).execute());
        if (!validStatusCode || response.isSuccess()) {
            log.debug("request api: {}, waste time: {} ms, parameters: {}, exchange body: {}", api.requestURL(), System.currentTimeMillis() - start, api.formData(), new String(response.body()));
            return response;
        }
        IoUtil.close(response);
        throw new ApiException(format("request failed with api: %s, status: %s, body: %s", api.requestURL(), response.code(), new String(response.body())));
    }

    public cn.hutool.http.HttpRequest buildRequest(HttpRequest<?> api) {
        cn.hutool.http.HttpRequest request = new cn.hutool.http.HttpRequest(api.requestURL())
                .setConnectionTimeout(api.connectTimeout())
                .setReadTimeout(api.readTimeout())
                .contentType(api.contentType())
                .method(this.resolveHttpMethod(api.method()))
                .headerMap(api.headers(), true)
                .setProxy(api.proxy());
        if (CollUtil.isNotEmpty(api.cookies())) {
            /* 必须先判断再设置，即使返回值是安全的 */
            request.cookie(api.cookies());
        }
        if (api.payload() != null) {
            return request.body(api.payload());
        }
        request.form(this.processFormData(api.formData()));
        if (Objects.equals(api.contentType(), ApiConstants.CONTENT_TYPE_JSON)) {
            request.body(JsonUtil.toJson(api.formData()));
        }
        return request;
    }

    public Method resolveHttpMethod(String method) {
        for (Method value : Method.values()) {
            if (value.name().equalsIgnoreCase(method)) {
                return value;
            }
        }
        throw new ApiException("unknown http request method: " + method);
    }

    protected Map<String, Object> processFormData(Map<String, Object> formData) {
        for (Map.Entry<String, Object> entry : formData.entrySet()) {
            if (entry.getValue() instanceof FilePart) {
                FilePart filePart = (FilePart) entry.getValue();
                entry.setValue(new InputStreamResource(filePart.openInputStream(), filePart.getName()));
            }
        }
        return formData;
    }

    public static class URLConnectionHttpResponse implements com.kfyty.sdk.api.core.http.HttpResponse {
        private final HttpResponse response;

        public URLConnectionHttpResponse(HttpResponse response) {
            this.response = Objects.requireNonNull(response);
        }

        @Override
        public int code() {
            return this.response.getStatus();
        }

        @Override
        public byte[] body() {
            return this.response.bodyBytes();
        }

        @Override
        public String header(String name) {
            return this.response.header(name);
        }

        @Override
        public String cookie(String name) {
            return this.response.getCookieValue(name);
        }

        @Override
        public Map<String, List<String>> headers() {
            return this.response.headers();
        }

        @Override
        public List<HttpCookie> cookies() {
            return this.response.getCookies();
        }

        @Override
        public void clearCookies() {
            GlobalCookieManager.getCookieManager().getCookieStore().removeAll();
        }

        @Override
        public Object source() {
            return this.response;
        }

        @Override
        public void close() {
            this.response.close();
        }
    }
}
