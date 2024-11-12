package com.kfyty.loveqq.framework.core.reflect;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 描述: {@link ParameterizedType} 实现，用于构建 {@link com.kfyty.loveqq.framework.core.generic.SimpleGeneric}
 *
 * @author kfyty725
 * @date 2022/7/24 13:18
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class DefaultParameterizedType implements ParameterizedType {
    private final Class<?> rawType;
    private final Class<?>[] classes;

    @Override
    public Type[] getActualTypeArguments() {
        return this.classes;
    }

    @Override
    public Type getRawType() {
        return this.rawType;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }

    @Override
    public String toString() {
        return CommonUtil.format("{}<{}>", this.rawType.getName(), Arrays.stream(this.classes).map(Class::getName).collect(Collectors.joining(", ")));
    }
}
