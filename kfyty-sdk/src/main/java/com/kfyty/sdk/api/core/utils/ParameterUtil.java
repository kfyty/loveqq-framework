package com.kfyty.sdk.api.core.utils;

import com.kfyty.sdk.api.core.ParameterConverter;
import com.kfyty.sdk.api.core.annotation.Parameter;
import com.kfyty.sdk.api.core.exception.ApiException;
import com.kfyty.support.utils.ReflectUtil;

import java.util.Optional;
import java.util.function.Supplier;

import static cn.hutool.core.text.CharSequenceUtil.isBlank;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;
import static java.lang.String.format;

/**
 * 描述: 参数处理工具类
 *
 * @author kun.zhang
 * @date 2021/11/15 17:50
 * @email kfyty725@hotmail.com
 */
public abstract class ParameterUtil {

    public static Optional<Object> resolveParameters(Parameter parameter, Object value) {
        return resolveParameters(parameter, value, null);
    }

    public static Optional<Object> resolveParameters(Parameter parameter, Object value, Supplier<Object> provider) {
        if (parameter == null) {
            return Optional.empty();
        }
        Object providerValue = null;
        if (parameter.require() && value == null && isBlank(parameter.defaultValue())) {
            if (provider == null || (providerValue = provider.get()) == null) {
                throw new ApiException(format("the field '%s' cannot be empty !", parameter.value()));
            }
        }
        if (value != null) {
            return Optional.of(value);
        }
        if (providerValue != null || (providerValue = provider.get()) != null) {
            return Optional.of(providerValue);
        }
        return isNotBlank(parameter.defaultValue()) ? Optional.of(parameter.defaultValue()) : Optional.empty();
    }

    public static String parameterConvert(Parameter parameter, Object value) {
        if (parameter == null || parameter.converter().equals(ParameterConverter.class)) {
            return value.toString();
        }
        return ReflectUtil.newInstance(parameter.converter()).doConvert(value);
    }
}
