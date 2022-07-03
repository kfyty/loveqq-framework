package com.kfyty.sdk.api.core.config;

import com.kfyty.sdk.api.core.ApiPostProcessor;
import com.kfyty.sdk.api.core.ApiPreProcessor;
import com.kfyty.sdk.api.core.ApiSerializer;
import com.kfyty.sdk.api.core.RequestFailedHandler;
import com.kfyty.sdk.api.core.constant.ApiConstants;
import com.kfyty.sdk.api.core.http.HttpRequestExecutor;
import com.kfyty.sdk.api.core.http.executor.URLConnectionHttpRequestExecutor;
import com.kfyty.sdk.api.core.serializer.JacksonApiSerializer;
import com.kfyty.sdk.api.core.support.ApiParametersPreProcessor;
import com.kfyty.sdk.api.core.support.ApiResponseValidPostProcessor;
import com.kfyty.sdk.api.core.support.ParameterProviderRegistry;
import com.kfyty.sdk.api.core.support.ThrowExceptionRequestFailedHandler;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 描述: api sdk 配置
 *
 * @author kfyty725
 * @date 2021/11/11 14:10
 * @email kfyty725@hotmail.com
 */
@Data
@Accessors(chain = true)
public class ApiConfiguration {
    /**
     * 适用于全局的配置
     */
    private volatile static ApiConfiguration globalConfiguration;

    /**
     * 基础 url
     */
    private String baseUrl;

    /**
     * 连接超时时间，毫秒
     */
    private int connectTimeout;

    /**
     * 请求超时时间，毫秒
     */
    private int readTimeout;

    /**
     * 请求前置处理器
     */
    private List<ApiPreProcessor> apiPreProcessors;

    /**
     * 请求后置处理器
     */
    private List<ApiPostProcessor> apiPostProcessors;

    /**
     * http 请求执行器
     */
    private HttpRequestExecutor requestExecutor;

    /**
     * 实现反序列化操作
     */
    private ApiSerializer apiSerializer;

    /**
     * 请求失败处理器
     */
    private RequestFailedHandler requestFailedHandler;

    /**
     * 参数提供器
     */
    private ParameterProviderRegistry parameterProviderRegistry;

    public ApiConfiguration addApiPreProcessor(ApiPreProcessor apiPreProcessor) {
        if (this.apiPreProcessors == null) {
            this.apiPreProcessors = new ArrayList<>();
        }
        this.apiPreProcessors.add(Objects.requireNonNull(apiPreProcessor));
        return this;
    }

    public ApiConfiguration addApiPostProcessor(ApiPostProcessor apiPostProcessor) {
        if (this.apiPostProcessors == null) {
            this.apiPostProcessors = new ArrayList<>();
        }
        this.apiPostProcessors.add(Objects.requireNonNull(apiPostProcessor));
        return this;
    }

    public static ApiConfiguration getGlobalConfiguration() {
        return getGlobalConfiguration(null);
    }

    public static ApiConfiguration getGlobalConfiguration(String baseUrl) {
        if (globalConfiguration == null) {
            synchronized (ApiConfiguration.class) {
                if (globalConfiguration == null) {
                    globalConfiguration = defaultConfiguration(baseUrl);
                }
            }
        }
        return globalConfiguration;
    }

    public static void setGlobalConfiguration(ApiConfiguration configuration) {
        ApiConfiguration.globalConfiguration = Objects.requireNonNull(configuration);
    }

    public static ApiConfiguration defaultConfiguration(String baseUrl) {
        return new ApiConfiguration()
                .setBaseUrl(baseUrl)
                .setConnectTimeout(ApiConstants.DEFAULT_CONNECT_REQUEST_TIME_OUT)
                .setReadTimeout(ApiConstants.DEFAULT_READ_REQUEST_TIME_OUT)
                .addApiPreProcessor(new ApiParametersPreProcessor())
                .addApiPostProcessor(new ApiResponseValidPostProcessor())
                .setRequestExecutor(new URLConnectionHttpRequestExecutor())
                .setApiSerializer(new JacksonApiSerializer())
                .setRequestFailedHandler(new ThrowExceptionRequestFailedHandler())
                .setParameterProviderRegistry(new ParameterProviderRegistry());
    }
}
