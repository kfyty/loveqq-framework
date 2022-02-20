package com.kfyty.sdk.api.core;

import com.kfyty.sdk.api.core.exception.ApiPostProcessorException;

/**
 * 描述: 请求失败处理器，用于后置处理
 * 抛出的异常类型一般为 {@link ApiPostProcessorException}
 *
 * @author kun.zhang
 * @date 2021/11/12 17:29
 * @email kfyty725@hotmail.com
 * @see AbstractApi#postProcessor(ApiResponse)
 */
public interface RequestFailedHandler {
    /**
     * 请求失败处理器
     *
     * @param api      请求失败的 api
     * @param response 失败的响应
     */
    void onFailed(AbstractApi<?, ?> api, ApiResponse response) throws ApiPostProcessorException;
}
