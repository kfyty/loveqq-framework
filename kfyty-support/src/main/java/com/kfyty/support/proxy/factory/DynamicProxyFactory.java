package com.kfyty.support.proxy.factory;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.BootApplication;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.NoArgsConstructor;

import static com.kfyty.support.utils.CommonUtil.EMPTY_CLASS_ARRAY;
import static com.kfyty.support.utils.CommonUtil.EMPTY_OBJECT_ARRAY;

/**
 * 描述: 动态代理工厂，用于创建代理对象
 *
 * @author kfyty725
 * @date 2021/6/19 11:50
 * @email kfyty725@hotmail.com
 */
@NoArgsConstructor
public abstract class DynamicProxyFactory {

    public static DynamicProxyFactory create(Object bean, ApplicationContext context) {
        if (!ReflectUtil.hasAnyInterfaces(bean.getClass()) || AnnotationUtil.hasAnnotationElement(bean, Configuration.class)) {
            return create(true);
        }
        BootApplication annotation = AnnotationUtil.findAnnotation(context.getPrimarySource(), BootApplication.class);
        return annotation == null ? create() : create(annotation.proxyTargetClass());
    }

    public static DynamicProxyFactory create() {
        return create(false);
    }

    public static DynamicProxyFactory create(boolean proxyTargetClass) {
        return !proxyTargetClass ? new JdkDynamicProxyFactory() : new CglibDynamicProxyFactory();
    }

    public Object createProxy(Object source) {
        return createProxy(source, EMPTY_CLASS_ARRAY, EMPTY_OBJECT_ARRAY);
    }

    public Object createProxy(Class<?> targetClass) {
        return createProxy(targetClass, EMPTY_CLASS_ARRAY, EMPTY_OBJECT_ARRAY);
    }

    public Object createProxy(Object source, BeanDefinition beanDefinition) {
        return createProxy(source, beanDefinition.getConstructArgTypes(), beanDefinition.getConstructArgValues());
    }

    public Object createProxy(Object source, Class<?>[] argTypes, Object[] argValues) {
        return createProxy(source, source.getClass(), argTypes, argValues);
    }

    public Object createProxy(Class<?> targetClass, Class<?>[] argTypes, Object[] argValues) {
        return createProxy(null, targetClass, argTypes, argValues);
    }

    public abstract Object createProxy(Object source, Class<?> targetClass, Class<?>[] argTypes, Object[] argValues);
}
