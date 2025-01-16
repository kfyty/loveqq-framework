package com.kfyty.loveqq.framework.aop.support.annotated;

import com.kfyty.loveqq.framework.aop.MethodMatcher;
import com.kfyty.loveqq.framework.aop.support.SimpleShadowMatch;
import org.aspectj.weaver.tools.ShadowMatch;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;

/**
 * 描述: 注解匹配器
 *
 * @author kfyty725
 * @date 2022/4/11 21:23
 * @email kfyty725@hotmail.com
 */
public class AnnotationMethodMatcher implements MethodMatcher {
    private final Class<? extends Annotation>[] annotationTypes;

    @SafeVarargs
    public AnnotationMethodMatcher(Class<? extends Annotation>... annotationType) {
        this.annotationTypes = annotationType;
    }

    @Override
    public ShadowMatch getShadowMatch(Method method) {
        return SimpleShadowMatch.INSTANCE;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        for (Class<? extends Annotation> annotationType : this.annotationTypes) {
            if (hasAnnotation(targetClass, annotationType) || hasAnnotation(method, annotationType)) {
                return true;
            }
        }
        return false;
    }
}
