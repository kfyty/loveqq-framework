package com.kfyty.loveqq.framework.core.lang.annotation;

import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.Getter;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 描述: 注解代理实现
 *
 * @author kfyty725
 * @date 2024/8/5 11:06
 * @email kfyty725@hotmail.com
 */
@Getter
public class AnnotationInvocationHandler implements InvocationHandler, Serializable {
    /**
     * 注解类型
     */
    private final Class<? extends Annotation> annotationType;

    /**
     * 注解元数据
     */
    private final Map<String, Object> memberValues;

    public AnnotationInvocationHandler(Class<? extends Annotation> annotationType) {
        this(annotationType, new LinkedHashMap<>(8));
    }

    public AnnotationInvocationHandler(Class<? extends Annotation> annotationType, Map<String, Object> memberValues) {
        this.annotationType = annotationType;
        this.memberValues = memberValues;
    }

    public AnnotationInvocationHandler put(String key, Object value) {
        this.memberValues.put(key, value);
        return this;
    }

    /**
     * @param proxy  the proxy instance that the method was invoked on
     * @param method the {@code Method} instance corresponding to
     *               the interface method invoked on the proxy instance.  The declaring
     *               class of the {@code Method} object will be the interface that
     *               the method was declared in, which may be a superinterface of the
     *               proxy interface that the proxy class inherits the method through.
     * @param args   an array of objects containing the values of the
     *               arguments passed in the method invocation on the proxy instance,
     *               or {@code null} if interface method takes no arguments.
     *               Arguments of primitive types are wrapped in instances of the
     *               appropriate primitive wrapper class, such as
     *               {@code java.lang.Integer} or {@code java.lang.Boolean}.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (ReflectUtil.isEquals(method)) {
            return this.equals(args[0]);
        }
        if (ReflectUtil.isHashCode(method)) {
            return this.hashCode();
        }
        if (ReflectUtil.isToString(method)) {
            return this.toString();
        }
        if (method.getName().equals("annotationType") && method.getParameterCount() == 0 && method.getReturnType() == Class.class) {
            return this.annotationType;
        }
        return this.memberValues.get(method.getName());
    }

    /**
     * 计算注解的 hash code，要保证和 {@link sun.reflect.annotation.AnnotationInvocationHandler#hashCodeImpl()} 相等
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        int result = 0;
        for (Map.Entry<String, Object> e : this.memberValues.entrySet()) {
            if (e.getValue() instanceof Object[] objects) {
                result += (127 * e.getKey().hashCode()) ^ Arrays.hashCode(objects);
            } else {
                result += (127 * e.getKey().hashCode()) ^ CommonUtil.hashCode(e.getValue());
            }
        }
        return result;
    }

    /**
     * 比较两个注解，要保证和 {@link sun.reflect.annotation.AnnotationInvocationHandler} 相等
     *
     * @param obj 对象
     * @return true/false
     */
    @Override
    public boolean equals(Object obj) {
        if (!this.annotationType.isInstance(obj)) {
            return false;
        }
        if (obj instanceof AnnotationInvocationHandler) {
            return Objects.equals(this.memberValues, ((AnnotationInvocationHandler) obj).memberValues);
        }
        Map<String, Object> memberValues = AnnotationUtil.getAnnotationValues((Annotation) obj);
        return Objects.equals(this.memberValues, memberValues);
    }

    /**
     * 返回字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        String values = this.memberValues.entrySet().stream().map(e -> e.getKey() + '=' + CommonUtil.toString(e.getValue())).collect(Collectors.joining(", "));
        return '@' + this.annotationType.getName() + '(' + values + ')';
    }
}
