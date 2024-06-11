package com.kfyty.loveqq.framework.sdk.api.core;

/**
 * 描述: 参数转换器
 *
 * @author kfyty725
 * @date 2021/11/24 17:37
 * @email kfyty725@hotmail.com
 */
public interface ParameterConverter<T, S> {
    /**
     * 将参数转换为字符串
     *
     * @param parameter 参数
     * @return 结果
     */
    S doConvert(T parameter);
}
