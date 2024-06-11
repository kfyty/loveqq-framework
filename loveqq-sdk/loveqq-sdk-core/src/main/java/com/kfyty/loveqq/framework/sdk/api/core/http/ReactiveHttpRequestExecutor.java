package com.kfyty.loveqq.framework.sdk.api.core.http;

import reactor.core.publisher.Mono;

/**
 * 描述: 响应式 http 请求执行器
 *
 * @author kfyty725
 * @date 2021/11/11 17:51
 * @email kfyty725@hotmail.com
 */
public interface ReactiveHttpRequestExecutor extends HttpRequestExecutor {
    /**
     * @see this#exchange(HttpRequest)
     */
    default Mono<HttpResponse> exchangeAsync(HttpRequest<?> api) {
        return this.exchangeAsync(api, true);
    }

    /**
     * @see this#exchange(HttpRequest, boolean)
     */
    Mono<HttpResponse> exchangeAsync(HttpRequest<?> api, boolean validStatusCode);

    /**
     * @see this#execute(HttpRequest)
     */
    default Mono<byte[]> executeAsync(HttpRequest<?> api) {
        return this.exchangeAsync(api).map(HttpResponse::body);
    }
}
