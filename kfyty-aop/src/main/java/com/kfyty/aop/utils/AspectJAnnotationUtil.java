package com.kfyty.aop.utils;

import com.kfyty.aop.AfterReturningAdvice;
import com.kfyty.aop.MethodAfterAdvice;
import com.kfyty.aop.MethodBeforeAdvice;
import com.kfyty.aop.MethodRoundAdvice;
import com.kfyty.aop.ThrowsAdvice;
import com.kfyty.aop.aspectj.AbstractAspectJAdvice;
import com.kfyty.aop.aspectj.AspectJAfterReturningAdvice;
import com.kfyty.aop.aspectj.AspectJAfterThrowsAdvice;
import com.kfyty.aop.aspectj.AspectJMethodAfterAdvice;
import com.kfyty.aop.aspectj.AspectJMethodBeforeAdvice;
import com.kfyty.aop.aspectj.AspectJMethodRoundAdvice;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
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
    @SuppressWarnings("unchecked")
    public static Class<? extends Annotation>[] ASPECT_ANNOTATION_TYPES = new Class[] {
            Around.class, Before.class, AfterReturning.class, AfterThrowing.class, After.class
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
        Class<? extends Annotation> annotationType = resolveAnnotationTypeFor(adviceType);
        return annotationType.equals(Around.class) || annotationType.equals(Before.class) ? -1 : 1;
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
        if (annotationType.equals(Around.class)) {
            return new AspectJMethodRoundAdvice();
        }
        if (annotationType.equals(Before.class)) {
            return new AspectJMethodBeforeAdvice();
        }
        if (annotationType.equals(AfterReturning.class)) {
            return new AspectJAfterReturningAdvice();
        }
        if (annotationType.equals(AfterThrowing.class)) {
            return new AspectJAfterThrowsAdvice();
        }
        if (annotationType.equals(After.class)) {
            return new AspectJMethodAfterAdvice();
        }
        throw new IllegalArgumentException("unsupported aspect annotation: " + annotationType);
    }

    public static Class<? extends Annotation> resolveAnnotationTypeFor(Class<?> adviceType) {
        Objects.requireNonNull(adviceType);
        if (MethodRoundAdvice.class.isAssignableFrom(adviceType)) {
            return Around.class;
        }
        if (MethodBeforeAdvice.class.isAssignableFrom(adviceType)) {
            return Before.class;
        }
        if (AfterReturningAdvice.class.isAssignableFrom(adviceType)) {
            return AfterReturning.class;
        }
        if (ThrowsAdvice.class.isAssignableFrom(adviceType)) {
            return AfterThrowing.class;
        }
        if (MethodAfterAdvice.class.isAssignableFrom(adviceType)) {
            return After.class;
        }
        throw new IllegalArgumentException("unsupported advice type: " + adviceType);
    }
}
