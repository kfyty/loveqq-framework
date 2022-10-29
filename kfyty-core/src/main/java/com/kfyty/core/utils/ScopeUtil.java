package com.kfyty.core.utils;

import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.Scope;
import com.kfyty.core.autoconfig.beans.BeanDefinition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 描述: 作用域工具
 *
 * @author kfyty725
 * @date 2021/8/6 20:40
 * @email kfyty725@hotmail.com
 */
public abstract class ScopeUtil {
    private static final Scope DEFAULT_SCOPE = new Scope() {

        @Override
        public String value() {
            return BeanDefinition.SCOPE_SINGLETON;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Scope.class;
        }
    };

    public static boolean isSingleton(Class<?> clazz) {
        return BeanDefinition.SCOPE_SINGLETON.equals(resolveScope(clazz).value());
    }

    public static boolean isSingleton(Method method) {
        return BeanDefinition.SCOPE_SINGLETON.equals(resolveScope(method).value());
    }

    public static Scope resolveScope(Class<?> clazz) {
        if (AnnotationUtil.hasAnnotationElement(clazz, Configuration.class)) {
            return defaultScope();
        }
        Scope scope = AnnotationUtil.findAnnotationElement(clazz, Scope.class);
        return scope != null ? scope : defaultScope();
    }

    public static Scope resolveScope(Method beanMethod) {
        Scope scope = AnnotationUtil.findAnnotationElement(beanMethod, Scope.class);
        return scope != null ? scope : defaultScope();
    }

    public static Scope defaultScope() {
        return DEFAULT_SCOPE;
    }
}
