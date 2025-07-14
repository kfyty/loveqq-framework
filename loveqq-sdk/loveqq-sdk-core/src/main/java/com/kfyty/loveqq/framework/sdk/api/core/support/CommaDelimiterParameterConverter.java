package com.kfyty.loveqq.framework.sdk.api.core.support;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.sdk.api.core.ParameterConverter;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

/**
 * 描述: 逗号分割的参数转换器
 *
 * @author kfyty725
 * @date 2021/11/24 17:50
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class CommaDelimiterParameterConverter implements ParameterConverter<Object, String> {
    /**
     * 默认实例
     */
    public static final ParameterConverter<Object, String> INSTANCE = new CommaDelimiterParameterConverter();

    @Override
    public String doConvert(Object parameter) {
        return CommonUtil.toList(parameter).stream().map(Object::toString).collect(Collectors.joining(","));
    }
}
