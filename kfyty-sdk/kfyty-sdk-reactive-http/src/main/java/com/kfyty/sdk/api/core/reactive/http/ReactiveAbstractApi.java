package com.kfyty.sdk.api.core.reactive.http;

import com.kfyty.sdk.api.core.AbstractConfigurableApi;
import com.kfyty.sdk.api.core.ApiResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 描述: api 响应式基础实现，封装了模板代码及扩展接口
 *
 * @author kfyty725
 * @date 2021/11/11 14:10
 * @email kfyty725@hotmail.com
 */
@Data
@Slf4j
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public abstract class ReactiveAbstractApi<T extends ReactiveAbstractApi<T, R>, R extends ApiResponse> extends AbstractConfigurableApi<T, R> {

    @Override
    public R exchange() {
        return this.exchangeAsync().block();
    }

    @Override
    public byte[] execute() {
        return this.executeAsync().block();
    }

    @Override
    public Mono<R> exchangeAsync() {
        long start = System.currentTimeMillis();
        this.preProcessor();
        return this.exchangeInternal()
                .doOnNext(response -> log.debug("request api: {}, waste time: {} ms, parameters: {}, exchange body: {}",
                        this.requestURL(),
                        System.currentTimeMillis() - start,
                        this.formData(),
                        new String(this.getConfiguration().getApiSerializer().serialize(response))))
                .doOnNext(this::postProcessor);
    }

    @Override
    public Mono<byte[]> executeAsync() {
        long start = System.currentTimeMillis();
        this.preProcessor();
        return this.executeInternal()
                .doOnNext(response -> log.debug("request api: {}, waste time: {} ms, parameters: {}, response body: {}", this.requestURL(), System.currentTimeMillis() - start, this.formData(), new String(response)));
    }

    protected Mono<byte[]> executeInternal() {
        return this.getConfiguration().getRequestExecutor().executeAsync(this);
    }

    protected Mono<R> exchangeInternal() {
        return this.getConfiguration()
                .getRequestExecutor()
                .exchangeAsync(this)
                .map(this::exchangeInternal);
    }
}
