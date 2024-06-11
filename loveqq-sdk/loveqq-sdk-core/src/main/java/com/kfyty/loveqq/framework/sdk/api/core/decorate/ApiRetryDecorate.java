package com.kfyty.loveqq.framework.sdk.api.core.decorate;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.sdk.api.core.Api;
import com.kfyty.loveqq.framework.sdk.api.core.ApiResponse;
import com.kfyty.loveqq.framework.sdk.api.core.exception.ApiException;
import com.kfyty.loveqq.framework.sdk.api.core.exception.ApiRetryException;
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
public class ApiRetryDecorate<T extends Api<T, R>, R extends ApiResponse> {
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
    private final Api<T, R> decorate;

    public ApiRetryDecorate(Api<T, R> api) {
        this.decorate = api;
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

    public R exchange() {
        return this.doRetry(this.decorate::exchange);
    }

    public byte[] execute() {
        return this.doRetry(this.decorate::execute);
    }

    public Object favorite() {
        return this.doRetry(this.decorate::favorite);
    }

    protected <RR> RR doRetry(Supplier<RR> result) {
        Throwable throwable = null;
        for (byte i = 0; i < retry; i++) {
            try {
                return result.get();
            } catch (ApiException e) {
                throwable = e;
                log.warn("failed to request api: {}, retry the {} times!", e, i + 1);
                CommonUtil.sleep(this.timeUnit.toMillis(this.sleep));
            }
        }
        throw new ApiRetryException("retried request api failed: " + throwable.getMessage(), throwable);
    }

    public static <T extends Api<T, R>, R extends ApiResponse> ApiRetryDecorate<T, R> of(Api<T, R> api) {
        return new ApiRetryDecorate<>(Objects.requireNonNull(api));
    }
}
