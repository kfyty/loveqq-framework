package com.kfyty.loveqq.framework.sdk.api.core.reactive.http;

import com.kfyty.loveqq.framework.sdk.api.core.AbstractCoreApi;
import com.kfyty.loveqq.framework.sdk.api.core.ApiResponse;
import com.kfyty.loveqq.framework.sdk.api.core.ReactiveApi;
import com.kfyty.loveqq.framework.sdk.api.core.decorate.ReactiveApiRetryDecorate;
import com.kfyty.loveqq.framework.sdk.api.core.exception.ApiException;
import com.kfyty.loveqq.framework.sdk.api.core.exception.BaseApiException;
import com.kfyty.loveqq.framework.sdk.api.core.http.HttpRequestExecutor;
import com.kfyty.loveqq.framework.sdk.api.core.http.ReactiveHttpRequestExecutor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import static java.lang.String.format;

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
public abstract class AbstractReactiveApi<T extends AbstractReactiveApi<T, R>, R extends ApiResponse> extends AbstractCoreApi<T, R> implements ReactiveApi<T, R> {

    @Override
    public Mono<byte[]> executeAsync() {
        return Mono.fromRunnable(this::preProcessor)
                .then(this.executeInternal())
                .onErrorMap(throwable -> {
                    if (throwable instanceof BaseApiException ae) {
                        return ae;
                    }
                    return new ApiException(format("request api: %s failed: %s", this.requestPath(), throwable.getMessage()), throwable);
                });
    }

    @Override
    public Mono<R> exchangeAsync() {
        return Mono.fromRunnable(this::preProcessor)
                .then(this.exchangeInternal())
                .doOnNext(this::postProcessor)
                .onErrorMap(throwable -> {
                    if (throwable instanceof BaseApiException ae) {
                        return ae;
                    }
                    return new ApiException(format("request api: %s failed: %s", this.requestPath(), throwable.getMessage()), throwable);
                });

    }

    /**
     * 返回重试的装饰 api
     *
     * @return 重试 api
     */
    public ReactiveApiRetryDecorate<T, R> reactiveRetried() {
        return ReactiveApiRetryDecorate.of(this);
    }

    public ReactiveHttpRequestExecutor getReactiveRequestExecutor() {
        HttpRequestExecutor requestExecutor = this.getConfiguration().getRequestExecutor();
        if (requestExecutor instanceof ReactiveHttpRequestExecutor executor) {
            return executor;
        }
        throw new IllegalArgumentException("Require ReactiveHttpRequestExecutor: " + requestExecutor);
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
