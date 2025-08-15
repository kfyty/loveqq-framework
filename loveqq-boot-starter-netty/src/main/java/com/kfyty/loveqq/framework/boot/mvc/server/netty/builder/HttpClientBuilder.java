package com.kfyty.loveqq.framework.boot.mvc.server.netty.builder;

import com.kfyty.loveqq.framework.core.autoconfig.BeanCustomizer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.util.function.Function;

/**
 * 描述: {@link HttpClient} 构建器，以支持 {@link BeanCustomizer} 自定义配置
 *
 * @author kfyty725
 * @date 2024/9/28 20:18
 * @email kfyty725@hotmail.com
 */
public class HttpClientBuilder {
    private HttpClient client;

    HttpClientBuilder() {
        this.client = HttpClient.create();
    }

    HttpClientBuilder(ConnectionProvider connectionProvider) {
        this.client = HttpClient.create(connectionProvider);
    }

    public HttpClientBuilder configure(Function<HttpClient, HttpClient> configure) {
        this.client = configure.apply(this.client);
        return this;
    }

    public HttpClient build() {
        return this.client;
    }

    public static HttpClientBuilder builder() {
        return new HttpClientBuilder();
    }

    public static HttpClientBuilder builder(ConnectionProvider connectionProvider) {
        return new HttpClientBuilder(connectionProvider);
    }
}
