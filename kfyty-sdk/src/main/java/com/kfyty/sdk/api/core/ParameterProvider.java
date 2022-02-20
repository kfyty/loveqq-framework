package com.kfyty.sdk.api.core;

/**
 * 描述: api 参数提供器
 *
 * @author kun.zhang
 * @date 2021/12/1 10:41
 * @email kfyty725@hotmail.com
 */
public interface ParameterProvider {
    /**
     * 提供参数
     *
     * @param api 本次应用的 api
     * @return 参数值
     */
    Object provide(AbstractApi<?, ?> api);
}
