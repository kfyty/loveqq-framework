package com.kfyty.loveqq.framework.sdk.api.core.decorate;

import com.kfyty.loveqq.framework.sdk.api.core.ApiResponse;
import com.kfyty.loveqq.framework.sdk.api.core.ReactiveApi;
import com.kfyty.loveqq.framework.sdk.api.core.exception.ApiException;
import com.kfyty.loveqq.framework.sdk.api.core.exception.ApiRetryException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 描述: api 重试装饰器
 *
 * <pre>{@code
 *      OauthCodeResponse exchange = ReactiveApiRetryDecorate.of(new OAuthAccessTokenApi())
 *                                      .withRetry(5)
 *                                      .exchange();
 * }</pre>
 *
 * @author kfyty725
 * @date 2021/11/12 15:10
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class ReactiveApiRetryDecorate<T extends ReactiveApi<T, R>, R extends ApiResponse> {
    /**
     * 重试次数
     * 默认 3 次
     */
    private int retry = 3;

    /**
     * 每次重试时睡眠的时间
     * 默认 500
     */
    private long sleep = 500;

    /**
     * 计算睡眠时间时的单位
     * 默认毫秒
     */
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    /**
     * api
     */
    private final ReactiveApi<T, R> decorate;

    public ReactiveApiRetryDecorate(ReactiveApi<T, R> api) {
        this.decorate = api;
    }

    public ReactiveApiRetryDecorate<T, R> withRetry(int retry) {
        this.retry = retry;
        return this;
    }

    public ReactiveApiRetryDecorate<T, R> withSleep(long sleep) {
        this.sleep = sleep;
        return this;
    }

    public ReactiveApiRetryDecorate<T, R> withTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public Mono<R> exchangeAsync() {
        return this.doRetry(0, this.decorate::exchangeAsync);
    }

    public Mono<byte[]> executeAsync() {
        return this.doRetry(0, this.decorate::executeAsync);
    }

    public Mono<Object> favoriteAsync() {
        return this.doRetry(0, this.decorate::favoriteAsync);
    }

    protected <RR> Mono<RR> doRetry(int currentRetry, Supplier<Mono<RR>> result) {
        return result.get()
                .onErrorResume(ApiException.class, ex -> {
                    if (currentRetry > this.retry) {
                        throw new ApiRetryException("retried request api failed: " + ex.getMessage(), ex);
                    }
                    log.warn("failed to request api: {}, retry the {} times!", ex, currentRetry + 1);
                    return Mono.delay(Duration.ofMillis(this.timeUnit.toMillis(this.sleep)))
                            .flatMap(e -> this.doRetry(currentRetry + 1, result));
                });
    }

    public static <T extends ReactiveApi<T, R>, R extends ApiResponse> ReactiveApiRetryDecorate<T, R> of(ReactiveApi<T, R> api) {
        return new ReactiveApiRetryDecorate<>(Objects.requireNonNull(api));
    }
}
