package com.kfyty.sdk.api.core;

/**
 * 描述: 参数转换器
 *
 * @author kun.zhang
 * @date 2021/11/24 17:37
 * @email kfyty725@hotmail.com
 */
public interface ParameterConverter {
    /**
     * 将参数转换为字符串
     *
     * @param parameter 参数
     * @return 结果
     */
    String doConvert(Object parameter);
}
