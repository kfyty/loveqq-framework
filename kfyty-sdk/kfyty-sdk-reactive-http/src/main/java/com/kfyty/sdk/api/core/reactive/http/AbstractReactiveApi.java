package com.kfyty.sdk.api.core.reactive.http;

import com.kfyty.sdk.api.core.AbstractConfigurableApi;
import com.kfyty.sdk.api.core.ApiResponse;
import com.kfyty.sdk.api.core.ReactorApi;
import com.kfyty.sdk.api.core.decorate.ReactiveApiRetryDecorate;
import com.kfyty.sdk.api.core.http.HttpRequestExecutor;
import com.kfyty.sdk.api.core.http.ReactiveHttpRequestExecutor;
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
public abstract class AbstractReactiveApi<T extends AbstractReactiveApi<T, R>, R extends ApiResponse> extends AbstractConfigurableApi<T, R> implements ReactorApi<T, R> {

    public ReactiveHttpRequestExecutor getReactiveRequestExecutor() {
        HttpRequestExecutor requestExecutor = this.getConfiguration().getRequestExecutor();
        if (!(requestExecutor instanceof ReactiveHttpRequestExecutor)) {
            throw new IllegalArgumentException("require ReactiveHttpRequestExecutor");
        }
        return (ReactiveHttpRequestExecutor) requestExecutor;
    }

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
        this.preProcessor();
        return this.exchangeInternal().doOnNext(this::postProcessor);
    }

    @Override
    public Mono<byte[]> executeAsync() {
        this.preProcessor();
        return this.executeInternal();
    }

    /**
     * 返回重试的装饰 api
     *
     * @return 重试 api
     */
    public ReactiveApiRetryDecorate<T, R> reactiveRetried() {
        return ReactiveApiRetryDecorate.of(this);
    }

    protected Mono<byte[]> executeInternal() {
        return this.getReactiveRequestExecutor().executeAsync(this);
    }

    protected Mono<R> exchangeInternal() {
        return this.getReactiveRequestExecutor()
                .exchangeAsync(this)
                .map(this::exchangeInternal);
    }
}
