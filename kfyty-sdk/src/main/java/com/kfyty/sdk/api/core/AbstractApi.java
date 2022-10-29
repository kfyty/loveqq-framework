package com.kfyty.sdk.api.core;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.kfyty.sdk.api.core.config.ApiConfiguration;
import com.kfyty.sdk.api.core.decorate.ApiRetryDecorate;
import com.kfyty.sdk.api.core.exception.ApiException;
import com.kfyty.sdk.api.core.exception.BaseApiException;
import com.kfyty.sdk.api.core.http.AbstractHttpRequest;
import com.kfyty.sdk.api.core.http.HttpResponse;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

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
public abstract class AbstractApi<T extends Api<T, R>, R extends ApiResponse> extends AbstractHttpRequest<T> implements Api<T, R> {
    /**
     * 适用于当前 api 的配置
     */
    private ApiConfiguration configuration;

    /**
     * 获取 base url
     *
     * @return base url
     */
    public String getBaseUrl() {
        return this.getConfiguration().getBaseUrl();
    }

    /**
     * 获取配置，如果未配置则使用全局配置
     *
     * @return ApiConfiguration
     */
    public ApiConfiguration getConfiguration() {
        return ofNullable(this.configuration).orElseGet(ApiConfiguration::getGlobalConfiguration);
    }

    /**
     * 克隆一份配置副本到本地，以自定义配置并消除对全局配置的修改
     *
     * @return this
     */
    public ApiConfiguration cloneConfig() {
        this.configuration = BeanUtil.copyProperties(this.getConfiguration(), ApiConfiguration.class);
        return this.getConfiguration();
    }

    /**
     * 解析父类的第二个泛型的类型
     *
     * @return 响应类型
     */
    @SuppressWarnings("unchecked")
    public Class<R> resolveResponseGeneric() {
        return (Class<R>) ReflectUtil.getSuperGeneric(this.getClass(), 1);
    }

    /**
     * 处理 URI 变量，提供给子类处理 {@link this#requestURL()} 返回值
     *
     * @param uri uri
     * @return url
     */
    public String processURIVariable(String uri) {
        Objects.requireNonNull(uri);
        return CommonUtil.processPlaceholder(uri, this.formData());
    }

    @Override
    public int connectTimeout() {
        return ofNullable(this.connectTimeout).orElseGet(() -> this.getConfiguration().getConnectTimeout());
    }

    @Override
    public int readTimeout() {
        return ofNullable(this.readTimeout).orElseGet(() -> this.getConfiguration().getReadTimeout());
    }

    @Override
    public void preProcessor() {
        List<ApiPreProcessor> apiPreProcessors = this.getConfiguration().getApiPreProcessors();
        if (CollUtil.isNotEmpty(apiPreProcessors)) {
            apiPreProcessors.forEach(e -> e.preProcessor(this));
        }
    }

    @Override
    public R exchange() {
        try {
            long start = DateUtil.current();
            this.preProcessor();
            R response = this.exchangeInternal();
            if (log.isDebugEnabled()) {
                log.debug("request api: {}, waste time: {} ms, parameters: {}, exchange body: {}", this.requestURL(), DateUtil.current() - start,
                        this.formData(), new String(this.getConfiguration().getApiSerializer().serialize(response)));
            }
            this.postProcessor(response);
            return response;
        } catch (BaseApiException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new ApiException(format("request api: %s failed: %s", this.requestURL(), throwable.getMessage()), throwable);
        }
    }

    @Override
    public byte[] execute() {
        try {
            long start = DateUtil.current();
            this.preProcessor();
            byte[] bytes = this.executeInternal();
            if (log.isDebugEnabled()) {
                log.debug("request api: {}, waste time: {} ms, parameters: {}, response body: {}", this.requestURL(), DateUtil.current() - start, this.formData(), new String(bytes));
            }
            return bytes;
        } catch (BaseApiException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new ApiException(format("request api: %s failed: %s", this.requestURL(), throwable.getMessage()), throwable);
        }
    }

    @Override
    public Object favorite() {
        return this.exchange();
    }

    @Override
    public void postProcessor(R response) {
        if (response == null) {
            throw new ApiException("response body is empty !");
        }
        List<ApiPostProcessor> apiPostProcessors = this.getConfiguration().getApiPostProcessors();
        if (CollUtil.isNotEmpty(apiPostProcessors)) {
            apiPostProcessors.forEach(e -> e.postProcessor(this, response));
        }
    }

    /**
     * 返回重试的装饰 api
     *
     * @return 重试 api
     */
    public ApiRetryDecorate<T, R> retried() {
        return ApiRetryDecorate.of(this);
    }

    protected byte[] executeInternal() {
        return this.getConfiguration().getRequestExecutor().execute(this);
    }

    protected R exchangeInternal() {
        try (HttpResponse response = this.getConfiguration().getRequestExecutor().wrapResponse(this)) {
            return this.exchangeInternal(response);
        }
    }

    @SuppressWarnings("unchecked")
    protected R exchangeInternal(HttpResponse response) {
        try {
            Class<R> responseGeneric = this.resolveResponseGeneric();
            R retValue = (R) this.getConfiguration().getApiSerializer().deserialize(response.body(), responseGeneric);
            return ofNullable(this.exchangeInternal(response, retValue)).orElse(retValue);
        } catch (BaseApiException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new ApiException("request api was successful, but failed during deserialization: " + throwable.getMessage(), throwable);
        }
    }

    protected R exchangeInternal(HttpResponse response, R retValue) {
        return retValue;
    }
}
