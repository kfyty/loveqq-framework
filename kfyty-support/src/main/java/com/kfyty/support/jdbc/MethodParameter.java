package com.kfyty.support.jdbc;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/3 15:43
 * @email kfyty725@hotmail.com
 */
@Data
@AllArgsConstructor
public class MethodParameter {
    /**
     * 参数所在的方法
     */
    private Method method;

    /**
     * 原始参数对象
     */
    private Parameter parameter;

    /**
     * 参数类型
     */
    private Class<?> paramType;

    /**
     * 参数泛型
     */
    private Type generic;

    /**
     * 参数值
     */
    private Object value;

    public MethodParameter(Method method, Parameter parameter) {
        this.method = method;
        this.parameter = parameter;
        this.paramType = parameter.getType();
        this.generic = parameter.getParameterizedType();
    }

    /**
     * 仅使用参数类型和参数值构造，一般用于后续使用
     * @param paramType 参数类型
     * @param value 参数值
     */
    public MethodParameter(Class<?> paramType, Object value) {
        this.paramType = paramType;
        this.value = value;
    }
}
