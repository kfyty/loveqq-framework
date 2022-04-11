package com.kfyty.aop.support.annotated;

import com.kfyty.aop.AnnotatedPointcut;
import com.kfyty.aop.MethodMatcher;

import java.lang.annotation.Annotation;

/**
 * 描述: 注解切入点实现
 *
 * @author kfyty725
 * @date 2022/4/11 21:24
 * @email kfyty725@hotmail.com
 */
public class AnnotationPointcut implements AnnotatedPointcut {
    private final Class<? extends Annotation> annotationType;

    public AnnotationPointcut(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return this.annotationType;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return new AnnotationMethodMatcher(this.annotationType());
    }
}
