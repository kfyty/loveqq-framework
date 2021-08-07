package com.kfyty.support.utils;

import com.kfyty.support.autoconfig.annotation.Scope;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.MethodBeanDefinition;

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

    public static boolean isSingleton(Class<?> clazz) {
        return BeanDefinition.SCOPE_SINGLETON.equals(resolveScope(clazz).value());
    }

    public static boolean isSingleton(Method method) {
        return BeanDefinition.SCOPE_SINGLETON.equals(resolveScope(method).value());
    }

    public static Scope resolveScope(Class<?> clazz) {
        Scope scope = AnnotationUtil.findAnnotation(clazz, Scope.class);
        return scope != null ? scope : defaultScope();
    }

    public static Scope resolveScope(Method beanMethod) {
        Scope scope = AnnotationUtil.findAnnotation(beanMethod, Scope.class);
        return scope != null ? scope : defaultScope();
    }

    public static Scope resolveScope(BeanDefinition beanDefinition) {
        if (beanDefinition instanceof MethodBeanDefinition) {
            return resolveScope(((MethodBeanDefinition) beanDefinition).getBeanMethod());
        }
        return resolveScope(beanDefinition.getBeanType());
    }

    public static Scope defaultScope() {
        return new Scope() {

            @Override
            public String value() {
                return BeanDefinition.SCOPE_SINGLETON;
            }

            @Override
            public boolean scopeProxy() {
                return false;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Scope.class;
            }
        };
    }
}
