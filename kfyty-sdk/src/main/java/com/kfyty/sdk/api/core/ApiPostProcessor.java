package com.kfyty.sdk.api.core;

/**
 * 描述: api 后置处理器，api 请求成功之后执行
 *
 * @author kfyty725
 * @date 2022/01/13 09:58
 * @email kfyty725@hotmail.com
 */
public interface ApiPostProcessor {
    /**
     * api 请求后置处理
     *
     * @param api      api
     * @param response 响应结果
     */
    void postProcessor(AbstractApi<?, ?> api, ApiResponse response);
}
