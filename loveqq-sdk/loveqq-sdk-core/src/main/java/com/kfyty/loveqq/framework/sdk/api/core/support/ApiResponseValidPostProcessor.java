package com.kfyty.loveqq.framework.sdk.api.core.support;

import com.kfyty.loveqq.framework.sdk.api.core.AbstractConfigurableApi;
import com.kfyty.loveqq.framework.sdk.api.core.ApiPostProcessor;
import com.kfyty.loveqq.framework.sdk.api.core.ApiResponse;

import java.util.Objects;

import static com.kfyty.loveqq.framework.sdk.api.core.enums.ErrorCodeEnum.SUCCESS;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/1/13 9:59
 * @email kfyty725@hotmail.com
 */
public class ApiResponseValidPostProcessor implements ApiPostProcessor {
    /**
     * api 请求后置处理
     *
     * @param api      api
     * @param response 响应结果
     */
    @Override
    public void postProcessor(AbstractConfigurableApi<?, ?> api, ApiResponse response) {
        if (!Objects.equals(SUCCESS.getCode(), response.getCode())) {
            api.getConfiguration().getRequestFailedHandler().onFailed(api, response);
        }
    }
}
