package com.kfyty.sdk.api.core.http;

import com.kfyty.sdk.api.core.AbstractConfigurableApi;
import com.kfyty.sdk.api.core.ApiResponse;
import com.kfyty.sdk.api.core.decorate.ApiRetryDecorate;
import com.kfyty.sdk.api.core.exception.ApiException;
import com.kfyty.sdk.api.core.exception.BaseApiException;
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
            long start = System.currentTimeMillis();
            this.preProcessor();
            R response = this.exchangeInternal();
            log.debug("request api: {}, waste time: {} ms, parameters: {}, exchange body: {}", this.requestURL(), System.currentTimeMillis() - start,
                    this.formData(), new String(this.getConfiguration().getApiSerializer().serialize(response)));
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
            long start = System.currentTimeMillis();
            this.preProcessor();
            byte[] bytes = this.executeInternal();
            log.debug("request api: {}, waste time: {} ms, parameters: {}, response body: {}", this.requestURL(), System.currentTimeMillis() - start, this.formData(), new String(bytes));
            return bytes;
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
