package com.kfyty.loveqq.framework.sdk.api.core.support;

import com.kfyty.loveqq.framework.sdk.api.core.AbstractCoreApi;
import com.kfyty.loveqq.framework.sdk.api.core.ApiResponse;
import com.kfyty.loveqq.framework.sdk.api.core.RequestFailedHandler;
import com.kfyty.loveqq.framework.sdk.api.core.exception.ApiPostProcessorException;
import lombok.extern.slf4j.Slf4j;

/**
 * 描述: 抛出异常处理器
 *
 * @author kfyty725
 * @date 2021/11/12 17:33
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class ThrowExceptionRequestFailedHandler implements RequestFailedHandler {
    /**
     * 默认实例
     */
    public static final RequestFailedHandler INSTANCE = new ThrowExceptionRequestFailedHandler();

    @Override
    public void onFailed(AbstractCoreApi<?, ?> api, ApiResponse response) throws ApiPostProcessorException {
        throw new ApiPostProcessorException(response.getCode(), response.getDesc());
    }
}
