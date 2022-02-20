package com.kfyty.sdk.api.core;

/**
 * 描述: api 前置处理器
 *
 * @author kun.zhang
 * @date 2021/11/23 17:40
 * @email kfyty725@hotmail.com
 */
public interface ApiPreProcessor {
    /**
     * api 请求前置处理
     *
     * @param api api
     */
    void preProcessor(AbstractApi<?, ?> api);
}
