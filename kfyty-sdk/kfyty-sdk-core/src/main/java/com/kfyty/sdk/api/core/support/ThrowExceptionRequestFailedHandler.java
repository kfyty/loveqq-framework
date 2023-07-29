package com.kfyty.sdk.api.core.support;

import com.kfyty.sdk.api.core.AbstractConfigurableApi;
import com.kfyty.sdk.api.core.ApiResponse;
import com.kfyty.sdk.api.core.RequestFailedHandler;
import com.kfyty.sdk.api.core.exception.ApiPostProcessorException;
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

    @Override
    public void onFailed(AbstractConfigurableApi<?, ?> api, ApiResponse response) throws ApiPostProcessorException {
        throw new ApiPostProcessorException(response.getCode(), response.getDesc());
    }
}
