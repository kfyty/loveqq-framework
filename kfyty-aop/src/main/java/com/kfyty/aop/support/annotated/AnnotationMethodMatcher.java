package com.kfyty.aop.support.annotated;

import com.kfyty.aop.MethodMatcher;
import com.kfyty.aop.support.SimpleShadowMatch;
import org.aspectj.weaver.tools.ShadowMatch;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static com.kfyty.core.utils.AnnotationUtil.hasAnnotation;

/**
 * 描述: 注解匹配器
 *
 * @author kfyty725
 * @date 2022/4/11 21:23
 * @email kfyty725@hotmail.com
 */
public class AnnotationMethodMatcher implements MethodMatcher {
    private final Class<? extends Annotation> annotationType;

    public AnnotationMethodMatcher(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    public ShadowMatch getShadowMatch(Method method) {
        return SimpleShadowMatch.INSTANCE;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return hasAnnotation(targetClass, this.annotationType) || hasAnnotation(method, this.annotationType);
    }
}
