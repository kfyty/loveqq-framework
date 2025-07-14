package com.kfyty.loveqq.framework.sdk.api.core;

import reactor.core.publisher.Mono;

/**
 * 描述: 响应式 api 顶层接口
 *
 * @author kfyty725
 * @date 2021/11/11 13:51
 * @email kfyty725@hotmail.com
 */
public interface ReactiveApi<T extends ReactiveApi<T, R>, R extends ApiResponse> extends Api<T, R> {
    /**
     * 同步适配
     */
    @Override
    default R exchange() {
        return this.exchangeAsync().block();
    }

    /**
     * 同步适配
     */
    @Override
    default byte[] execute() {
        return this.executeAsync().block();
    }

    /**
     * 异步执行请求
     *
     * @see this#exchange()
     */
    Mono<R> exchangeAsync();

    /**
     * 异步执行请求
     *
     * @see this#execute()
     */
    Mono<byte[]> executeAsync();

    /**
     * 异步执行请求
     *
     * @see this#favorite()
     */
    default Mono<Object> favoriteAsync() {
        return this.exchangeAsync().map(e -> e);
    }
}
