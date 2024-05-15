package com.kfyty.aop.utils;

import com.kfyty.core.proxy.aop.AfterReturningAdvice;
import com.kfyty.core.proxy.aop.MethodAfterAdvice;
import com.kfyty.core.proxy.aop.MethodAroundAdvice;
import com.kfyty.core.proxy.aop.MethodBeforeAdvice;
import com.kfyty.core.proxy.aop.ThrowingAdvice;
import com.kfyty.aop.aspectj.AbstractAspectJAdvice;
import com.kfyty.aop.aspectj.AspectJAfterReturningAdvice;
import com.kfyty.aop.aspectj.AspectJThrowingAdvice;
import com.kfyty.aop.aspectj.AspectJMethodAfterAdvice;
import com.kfyty.aop.aspectj.AspectJMethodAroundAdvice;
import com.kfyty.aop.aspectj.AspectJMethodBeforeAdvice;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;

/**
 * 描述: 切面注解工具
 *
 * @author kfyty725
 * @date 2021/7/29 12:06
 * @email kfyty725@hotmail.com
 */
public abstract class AspectJAnnotationUtil {
    /**
     * 支持的注解类型，该顺序也是通知顺序
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends Annotation>[] ASPECT_ANNOTATION_TYPES = new Class[] {
            Around.class, Before.class, After.class, AfterReturning.class, AfterThrowing.class
    };

    public static String[] findArgNames(Method method) {
        Annotation annotation = findAspectAnnotation(method);
        if (annotation != null) {
            String argNames = ReflectUtil.invokeMethod(annotation, "argNames");
            if (CommonUtil.notEmpty(argNames)) {
                return Arrays.stream(argNames.split(",")).map(String::trim).toArray(String[]::new);
            }
        }
        return Arrays.stream(method.getParameters()).map(Parameter::getName).toArray(String[]::new);
    }

    public static Annotation findAspectAnnotation(Method method) {
        for (Class<? extends Annotation> annotationType : ASPECT_ANNOTATION_TYPES) {
            Annotation annotation = AnnotationUtil.findAnnotation(method, annotationType);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    public static int findAspectOrder(Class<?> adviceType) {
        int index = 0;
        Class<? extends Annotation> annotationType = resolveAnnotationTypeFor(adviceType);
        for (Class<? extends Annotation> aspectType : ASPECT_ANNOTATION_TYPES) {
            if (aspectType == annotationType) {
                return index;
            }
            index++;
        }
        return 99;
    }

    public static String findAspectExpression(Method method) {
        Annotation aspectAnnotation = findAspectAnnotation(method);
        if (aspectAnnotation == null) {
            return null;
        }
        String expression = ReflectUtil.invokeMethod(aspectAnnotation, "value");
        if (CommonUtil.empty(expression)) {
            Method pointcut = ReflectUtil.getMethod(aspectAnnotation.annotationType(), "pointcut");
            if (pointcut != null) {
                expression = (String) ReflectUtil.invokeMethod(aspectAnnotation, pointcut);
            }
        }
        return CommonUtil.empty(expression) ? null : expression;
    }

    public static AbstractAspectJAdvice resolveAspectFor(Class<?> annotationType) {
        Objects.requireNonNull(annotationType);
        if (annotationType.equals(Before.class)) {
            return new AspectJMethodBeforeAdvice();
        }
        if (annotationType.equals(Around.class)) {
            return new AspectJMethodAroundAdvice();
        }
        if (annotationType.equals(AfterReturning.class)) {
            return new AspectJAfterReturningAdvice();
        }
        if (annotationType.equals(AfterThrowing.class)) {
            return new AspectJThrowingAdvice();
        }
        if (annotationType.equals(After.class)) {
            return new AspectJMethodAfterAdvice();
        }
        throw new IllegalArgumentException("unsupported aspect annotation: " + annotationType);
    }

    public static Class<? extends Annotation> resolveAnnotationTypeFor(Class<?> adviceType) {
        Objects.requireNonNull(adviceType);
        if (MethodBeforeAdvice.class.isAssignableFrom(adviceType)) {
            return Before.class;
        }
        if (MethodAroundAdvice.class.isAssignableFrom(adviceType)) {
            return Around.class;
        }
        if (AfterReturningAdvice.class.isAssignableFrom(adviceType)) {
            return AfterReturning.class;
        }
        if (ThrowingAdvice.class.isAssignableFrom(adviceType)) {
            return AfterThrowing.class;
        }
        if (MethodAfterAdvice.class.isAssignableFrom(adviceType)) {
            return After.class;
        }
        throw new IllegalArgumentException("unsupported advice type: " + adviceType);
    }
}
