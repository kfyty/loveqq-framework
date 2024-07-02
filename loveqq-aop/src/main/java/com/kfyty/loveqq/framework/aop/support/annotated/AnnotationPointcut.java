package com.kfyty.loveqq.framework.aop.support.annotated;

import com.kfyty.loveqq.framework.aop.AnnotatedPointcut;
import com.kfyty.loveqq.framework.aop.MethodMatcher;

import java.lang.annotation.Annotation;

/**
 * 描述: 注解切入点实现
 *
 * @author kfyty725
 * @date 2022/4/11 21:24
 * @email kfyty725@hotmail.com
 */
public class AnnotationPointcut implements AnnotatedPointcut {
    private final Class<? extends Annotation>[] annotationType;

    @SafeVarargs
    public AnnotationPointcut(Class<? extends Annotation>... annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        throw new UnsupportedOperationException("See this.annotationTypes()");
    }

    public Class<? extends Annotation>[] annotationTypes() {
        return this.annotationType;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return new AnnotationMethodMatcher(this.annotationTypes());
    }
}
