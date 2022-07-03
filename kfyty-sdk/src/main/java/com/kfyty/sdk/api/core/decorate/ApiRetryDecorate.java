package com.kfyty.sdk.api.core.decorate;

import cn.hutool.core.thread.ThreadUtil;
import com.kfyty.sdk.api.core.AbstractApi;
import com.kfyty.sdk.api.core.Api;
import com.kfyty.sdk.api.core.ApiResponse;
import com.kfyty.sdk.api.core.exception.ApiException;
import com.kfyty.sdk.api.core.exception.ApiRetryException;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 描述: api 重试装饰器
 *
 * <pre>{@code
 *      OauthCodeResponse exchange = ApiRetryDecorate.of(new OAuthAccessTokenApi())
 *                                      .withRetry(5)
 *                                      .exchange();
 * }</pre>
 *
 * @author kfyty725
 * @date 2021/11/12 15:10
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class ApiRetryDecorate<T extends Api<T, R>, R extends ApiResponse> extends ApiDecorate<T, R> {
    /**
     * 重试次数
     * 默认 3 次
     */
    private int retry = 3;

    /**
     * 每次重试时睡眠的时间
     * 默认 100
     */
    private long sleep = 200;

    /**
     * 计算睡眠时间时的单位
     * 默认毫秒
     */
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    public ApiRetryDecorate(AbstractApi<T, R> api) {
        super(api);
    }

    public ApiRetryDecorate<T, R> withRetry(int retry) {
        this.retry = retry;
        return this;
    }

    public ApiRetryDecorate<T, R> withSleep(long sleep) {
        this.sleep = sleep;
        return this;
    }

    public ApiRetryDecorate<T, R> withTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public R exchange() {
        return (R) this.doRetry(() -> this.decorate.exchange());
    }

    @Override
    public byte[] execute() {
        return (byte[]) this.doRetry(() -> this.decorate.execute());
    }

    @Override
    public Object favorite() {
        return this.doRetry(() -> this.decorate.favorite());
    }

    private Object doRetry(Supplier<Object> result) {
        Throwable throwable = null;
        for (byte i = 0; i < retry; i++) {
            try {
                return result.get();
            } catch (ApiException e) {
                throwable = e;
                log.warn("failed to request api: {}, retry the {} times!", e, i + 1);
                ThreadUtil.sleep(this.timeUnit.toMillis(this.sleep));
            }
        }
        throw new ApiRetryException("retried request api failed: " + throwable.getMessage(), throwable);
    }

    public static <T extends Api<T, R>, R extends ApiResponse> ApiRetryDecorate<T, R> of(AbstractApi<T, R> api) {
        return new ApiRetryDecorate<>(Objects.requireNonNull(api));
    }
}
