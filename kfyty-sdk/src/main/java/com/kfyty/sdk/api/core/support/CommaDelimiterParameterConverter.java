package com.kfyty.sdk.api.core.support;

import com.kfyty.sdk.api.core.ParameterConverter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 描述: 逗号分割的参数转换器
 *
 * @author kfyty725
 * @date 2021/11/24 17:50
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class CommaDelimiterParameterConverter implements ParameterConverter {

    @Override
    public String doConvert(Object parameter) {
        if (!(parameter instanceof Collection)) {
            log.warn("parameter conversion failed, the parameter is not a collection type: {}", parameter);
            return parameter.toString();
        }
        return ((Collection<?>) parameter).stream().map(Object::toString).collect(Collectors.joining(","));
    }
}
