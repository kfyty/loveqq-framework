package com.kfyty.loveqq.framework.core.method;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.Function;

/**
 * 描述: 描述方法参数，也可用于单独描述方法
 *
 * @author kfyty725
 * @date 2021/6/3 15:43
 * @email kfyty725@hotmail.com
 */
@Data
@AllArgsConstructor
public class MethodParameter implements Cloneable {
    /**
     * 方法所在实例
     */
    private Object source;

    /**
     * 参数所在的方法
     */
    private Method method;

    /**
     * 方法参数
     */
    private Object[] methodArgs;

    /**
     * 方法参数
     * 该方法参数更详细
     */
    private MethodParameter[] methodParameters;

    /**
     * 方法参数对象
     */
    private Parameter parameter;

    /**
     * 方法参数名称
     */
    private String paramName;

    /**
     * 方法参数类型
     */
    private Class<?> paramType;

    /**
     * 方法返回值类型
     */
    private Class<?> returnType;

    /**
     * 方法参数泛型
     */
    private Type parameterGeneric;

    /**
     * 方法返回值泛型
     */
    private Type returnGeneric;

    /**
     * 参数值，用于描述方法参数时有效
     */
    private Object value;

    /**
     * 拓展元数据
     */
    private Object metadata;

    public MethodParameter(Method method) {
        this.method = method;
        this.returnType = method.getReturnType();
        this.returnGeneric = method.getGenericReturnType();
    }

    public MethodParameter(Object source, Method method) {
        this(method);
        this.source = source;
    }

    public MethodParameter(Method method, MethodParameter... methodParameters) {
        this(null, method, Arrays.stream(methodParameters).map(MethodParameter::getValue).toArray());
        this.methodParameters = methodParameters;
    }

    public MethodParameter(Object source, Method method, Object... methodArgs) {
        this(source, method);
        this.methodArgs = methodArgs;
    }

    public MethodParameter(Method method, Parameter parameter) {
        this(null, method, parameter);
    }

    public MethodParameter(Object source, Method method, Parameter parameter) {
        this(method);
        this.source = source;
        this.parameter = parameter;
        this.paramName = parameter.getName();
        this.paramType = parameter.getType();
        this.parameterGeneric = parameter.getParameterizedType();
    }

    /**
     * 仅使用参数类型和参数值构造，一般用于后续使用
     *
     * @param paramType 参数类型
     * @param value     参数值
     */
    public MethodParameter(Class<?> paramType, Object value) {
        this(paramType, value, null);
    }

    /**
     * 仅使用参数类型和参数值构造，一般用于后续使用
     *
     * @param paramType 参数类型
     * @param value     参数值
     * @param paramName 方法参数名称
     */
    public MethodParameter(Class<?> paramType, Object value, String paramName) {
        this.paramType = paramType;
        this.value = value;
        this.paramName = paramName;
    }

    /**
     * 使用声明方法、原参数对象和参数值构造
     *
     * @param method    声明方法
     * @param parameter 原参数对象
     * @param value     方法参数值
     */
    public MethodParameter(Method method, Parameter parameter, Object value) {
        this(method, parameter, value, parameter.getName());
    }

    /**
     * 使用声明方法、原参数对象和参数值构造
     *
     * @param method    声明方法
     * @param parameter 原参数对象
     * @param value     方法参数值
     * @param paramName 方法参数名称
     */
    public MethodParameter(Method method, Parameter parameter, Object value, String paramName) {
        this(method, parameter);
        this.value = value;
        this.paramName = paramName;
    }

    /**
     * 获取方法参数名称，优先从提供者中获取
     *
     * @param annotation 方法参数注解
     * @param mapping    参数名称提供者
     * @return 参数名称
     */
    public <A extends Annotation> String getParameterName(A annotation, Function<A, String> mapping) {
        String declaringName = annotation == null ? null : mapping.apply(annotation);
        return CommonUtil.notEmpty(declaringName) ? declaringName : this.getParamName();
    }

    /**
     * 设置元数据供后续自定义使用
     *
     * @param metadata 元数据
     * @return this
     */
    public MethodParameter metadata(Object metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public MethodParameter clone() {
        try {
            return (MethodParameter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new ResolvableException(e);
        }
    }
}
