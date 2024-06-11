package com.kfyty.loveqq.framework.sdk.api.core.http;

import com.kfyty.loveqq.framework.sdk.api.core.AbstractConfigurableApi;
import com.kfyty.loveqq.framework.sdk.api.core.ApiResponse;
import com.kfyty.loveqq.framework.sdk.api.core.decorate.ApiRetryDecorate;
import com.kfyty.loveqq.framework.sdk.api.core.exception.ApiException;
import com.kfyty.loveqq.framework.sdk.api.core.exception.BaseApiException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static java.lang.String.format;

/**
 * 描述: api 基础实现，封装了模板代码及扩展接口
 * 如果需要对 api 调用进行重试的操作，可以使用 {@link ApiRetryDecorate} 进行装饰
 *
 * @author kfyty725
 * @date 2021/11/11 14:10
 * @email kfyty725@hotmail.com
 */
@Data
@Slf4j
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractApi<T extends AbstractApi<T, R>, R extends ApiResponse> extends AbstractConfigurableApi<T, R> {

    @Override
    public R exchange() {
        try {
            this.preProcessor();
            R response = this.exchangeInternal();
            this.postProcessor(response);
            return response;
        } catch (BaseApiException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new ApiException(format("request api: %s failed: %s", this.requestPath(), throwable.getMessage()), throwable);
        }
    }

    @Override
    public byte[] execute() {
        try {
            this.preProcessor();
            return this.executeInternal();
        } catch (BaseApiException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new ApiException(format("request api: %s failed: %s", this.requestPath(), throwable.getMessage()), throwable);
        }
    }

    protected byte[] executeInternal() {
        return this.getConfiguration().getRequestExecutor().execute(this);
    }

    protected R exchangeInternal() {
        try (HttpResponse response = this.getConfiguration().getRequestExecutor().exchange(this)) {
            return this.exchangeInternal(response);
        }
    }
}
