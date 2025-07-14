package com.kfyty.loveqq.framework.sdk.api.core;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.sdk.api.core.config.ApiConfiguration;
import com.kfyty.loveqq.framework.sdk.api.core.decorate.ApiRetryDecorate;
import com.kfyty.loveqq.framework.sdk.api.core.exception.ApiException;
import com.kfyty.loveqq.framework.sdk.api.core.exception.BaseApiException;
import com.kfyty.loveqq.framework.sdk.api.core.http.AbstractHttpRequest;
import com.kfyty.loveqq.framework.sdk.api.core.http.HttpResponse;
import com.kfyty.loveqq.framework.sdk.api.core.http.HttpResponseAware;

import java.util.List;

import static java.util.Optional.ofNullable;

/**
 * 描述: 可配置的抽象 api 基础实现
 *
 * @author kfyty725
 * @date 2021/11/11 14:10
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractCoreApi<T extends AbstractCoreApi<T, R>, R extends ApiResponse> extends AbstractHttpRequest<T> implements Api<T, R> {
    /**
     * 适用于当前 api 的配置
     */
    protected ApiConfiguration configuration;

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
        this.configuration = this.configuration.clone();
        return this.configuration;
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

    @Override
    public String requestPath() {
        String baseUrl = this.getBaseUrl();
        if (baseUrl == null) {
            return super.requestPath();
        }
        return baseUrl + super.requestPath();
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
        if (CommonUtil.notEmpty(apiPreProcessors)) {
            apiPreProcessors.forEach(e -> e.preProcessor(this));
        }
    }

    @Override
    public void postProcessor(R response) {
        if (response == null) {
            throw new ApiException("Response body is empty !");
        }
        List<ApiPostProcessor> apiPostProcessors = this.getConfiguration().getApiPostProcessors();
        if (CommonUtil.notEmpty(apiPostProcessors)) {
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
        if (this instanceof HttpResponseAware aware) {
            aware.setHttpResponse(response);
        }
        if (retValue instanceof HttpResponseAware aware) {
            aware.setHttpResponse(response);
        }
        return retValue;
    }
}
