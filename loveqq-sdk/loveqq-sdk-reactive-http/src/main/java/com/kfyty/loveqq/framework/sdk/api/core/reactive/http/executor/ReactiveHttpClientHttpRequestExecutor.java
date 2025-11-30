package com.kfyty.loveqq.framework.sdk.api.core.reactive.http.executor;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import com.kfyty.loveqq.framework.sdk.api.core.Api;
import com.kfyty.loveqq.framework.sdk.api.core.constant.ApiConstants;
import com.kfyty.loveqq.framework.sdk.api.core.exception.ApiException;
import com.kfyty.loveqq.framework.sdk.api.core.http.HttpRequest;
import com.kfyty.loveqq.framework.sdk.api.core.http.HttpResponse;
import com.kfyty.loveqq.framework.sdk.api.core.http.ReactiveHttpRequestExecutor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.net.HttpCookie;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 描述: 基于 {@link java.net.http.HttpClient} 的执行器
 *
 * @author kfyty725
 * @date 2021/11/11 18:12
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class ReactiveHttpClientHttpRequestExecutor implements ReactiveHttpRequestExecutor {
    /**
     * 默认实例
     */
    public static final ReactiveHttpRequestExecutor INSTANCE = new ReactiveHttpClientHttpRequestExecutor();

    /**
     * 这里以 {@link ProxySelector} 作为 key，连接超时则以第一个请求的 {@link Api#connectTimeout()} 为准，
     * 原因是 jdk http client 不可变，这里仅针对代理维护缓存，连接超时配置不再额外维护缓存
     * {@link HttpClient}
     */
    protected static final Map<ProxySelector, HttpClient> HTTP_CLIENT_CACHE;

    /**
     * 这里关闭时过滤是因为有的 jdk 版本实现了 {@link AutoCloseable} 有的没有实现
     */
    static {
        HTTP_CLIENT_CACHE = new ConcurrentHashMap<>(4);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> HTTP_CLIENT_CACHE.values().stream().filter(e -> e instanceof AutoCloseable).forEach(IOUtil::close)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public HttpResponse wrapResponse(Object response) {
        return new ReactiveHttpResponse((java.net.http.HttpResponse<byte[]>) response);
    }

    @Override
    public Mono<HttpResponse> exchangeAsync(HttpRequest<?> api, boolean validStatusCode) {
        return Mono.fromCompletionStage(() -> {
            long start = System.currentTimeMillis();
            HttpClient client = this.obtainHttpClient(api);
            return client
                    .sendAsync(this.buildHttpRequest(api), java.net.http.HttpResponse.BodyHandlers.ofByteArray())
                    .thenApplyAsync(this::wrapResponse)
                    .whenComplete((response, ex) -> {
                        if (ex != null) {
                            throw new ApiException(ex.getMessage(), ex);
                        }
                        if (validStatusCode && !response.isSuccess()) {
                            throw new ApiException(format("request failed with api: %s, status: %s, body: %s", api.requestURL(), response.code(), new String(response.body())));
                        }
                        log.debug("request api: {}, waste time: {} ms, parameters: {}, exchange body: {}", api.requestURL(), System.currentTimeMillis() - start, api.formData(), new String(response.body()));
                    });
        });
    }

    protected HttpClient obtainHttpClient(HttpRequest<?> api) {
        return HTTP_CLIENT_CACHE.computeIfAbsent(api.proxySelector(), k ->
                this.postProcessClient(
                                HttpClient.newBuilder()
                                        .version(HttpClient.Version.HTTP_2)
                                        .connectTimeout(Duration.ofMillis(api.connectTimeout()))
                                        .proxy(api.proxySelector())
                        )
                        .build());
    }

    protected java.net.http.HttpRequest buildHttpRequest(HttpRequest<?> api) {
        java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create(api.requestURL()))
                .timeout(Duration.ofMillis(api.readTimeout()));

        for (Map.Entry<String, String> entry : api.headers().entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }

        if (CommonUtil.notEmpty(api.cookies())) {
            String cookie = api.cookies().stream().map(HttpCookie::toString).collect(Collectors.joining("; "));
            builder.header("Cookie", cookie);
        }

        builder.header("Content-Type", api.contentType());

        switch (api.method()) {
            case "GET" -> builder.GET();
            case "DELETE" -> builder.DELETE();
            case "POST", "PUT" -> {
                if (api.payload() != null && api.payload().length > 0) {
                    builder.method(api.method(), java.net.http.HttpRequest.BodyPublishers.ofByteArray(api.payload()));
                } else if (Objects.equals(api.contentType(), ApiConstants.CONTENT_TYPE_JSON)) {
                    builder.method(api.method(), java.net.http.HttpRequest.BodyPublishers.ofString(JsonUtil.toJSONString(api.formData())));
                } else {
                    String formData = api.formData().entrySet().stream().map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue().toString(), UTF_8)).collect(Collectors.joining("&"));
                    builder.method(api.method(), java.net.http.HttpRequest.BodyPublishers.ofString(formData));
                }
            }
        }

        return this.postProcessRequest(builder).build();
    }

    protected HttpClient.Builder postProcessClient(HttpClient.Builder builder) {
        return builder;
    }

    protected java.net.http.HttpRequest.Builder postProcessRequest(java.net.http.HttpRequest.Builder builder) {
        return builder;
    }

    public static class ReactiveHttpResponse implements HttpResponse {
        private final java.net.http.HttpResponse<byte[]> response;

        public ReactiveHttpResponse(java.net.http.HttpResponse<byte[]> response) {
            this.response = Objects.requireNonNull(response);
        }

        @Override
        public int code() {
            return this.response.statusCode();
        }

        @Override
        public byte[] body() {
            return this.response.body();
        }

        @Override
        public String header(String name) {
            return this.response.headers().firstValue(name).orElse(null);
        }

        @Override
        public String cookie(String name) {
            return this.cookies().stream().filter(e -> Objects.equals(e.getName(), name)).findAny().map(HttpCookie::getValue).orElse(null);
        }

        @Override
        public Map<String, List<String>> headers() {
            return this.response.headers().map();
        }

        @Override
        public List<HttpCookie> cookies() {
            String cookie = this.header("Set-Cookie");
            return CommonUtil.split(cookie, ";").stream().map(e -> e.split("=")).map(e -> new HttpCookie(e[0].trim(), e[1].trim())).collect(Collectors.toList());
        }

        @Override
        public void clearCookies() {
            // nothing
        }

        @Override
        public Object source() {
            return this.response;
        }
    }
}
