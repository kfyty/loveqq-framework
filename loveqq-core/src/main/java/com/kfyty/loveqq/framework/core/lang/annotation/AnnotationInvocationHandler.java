package com.kfyty.loveqq.framework.core.lang.annotation;

import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
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
public class AnnotationInvocationHandler implements InvocationHandler {
    /**
     * 注解类型
     */
    private final Class<? extends Annotation> annotationType;

    /**
     * 注解元数据
     */
    private final Map<String, Object> memberValues;

    public AnnotationInvocationHandler(Class<? extends Annotation> annotationType) {
        this(annotationType, new HashMap<>(8));
    }

    public AnnotationInvocationHandler(Class<? extends Annotation> annotationType, Map<String, Object> memberValues) {
        this.annotationType = annotationType;
        this.memberValues = memberValues;
    }

    public AnnotationInvocationHandler put(String key, Object value) {
        this.memberValues.put(key, value);
        return this;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(this.annotationType, this.memberValues);
    }

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

    @Override
    public String toString() {
        String values = this.memberValues.entrySet().stream().map(e -> e.getKey() + '=' + CommonUtil.toString(e.getValue())).collect(Collectors.joining(", "));
        return '@' + this.annotationType.getName() + '(' + values + ')';
    }
}
