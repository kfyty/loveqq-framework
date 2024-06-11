package com.kfyty.loveqq.framework.sdk.api.core;

import reactor.core.publisher.Mono;

/**
 * 描述: 响应式 api 顶层接口
 *
 * @author kfyty725
 * @date 2021/11/11 13:51
 * @email kfyty725@hotmail.com
 */
public interface ReactorApi<T extends ReactorApi<T, R>, R extends ApiResponse> extends Api<T, R> {
    /**
     * 异步执行请求
     *
     * @see this#exchange()
     */
    default Mono<R> exchangeAsync() {
        return Mono.fromCallable(this::exchange);
    }

    /**
     * 异步执行请求
     *
     * @see this#execute()
     */
    default Mono<byte[]> executeAsync() {
        return Mono.fromCallable(this::execute);
    }

    /**
     * 异步执行请求
     *
     * @see this#favorite()
     */
    default Mono<Object> favoriteAsync() {
        return this.exchangeAsync().map(e -> e);
    }
}
