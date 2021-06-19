package com.kfyty.support.proxy.factory;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.BootApplication;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * 描述: 动态代理工厂，用于创建代理对象
 *
 * @author kfyty725
 * @date 2021/6/19 11:50
 * @email kfyty725@hotmail.com
 */
@NoArgsConstructor
public abstract class DynamicProxyFactory {

    public static DynamicProxyFactory create() {
        return create(true);
    }

    public static DynamicProxyFactory create(Object bean, ApplicationContext context) {
        if(!ReflectUtil.hasAnyInterfaces(bean.getClass())) {
            return create(true);
        }
        for (Method method : bean.getClass().getMethods()) {
            if(AnnotationUtil.hasAnnotation(method, Bean.class)) {
                return create(true);
            }
        }
        BootApplication annotation = AnnotationUtil.findAnnotation(context.getPrimarySource(), BootApplication.class);
        return annotation == null ? create() : create(annotation.proxyTargetClass());
    }

    public static DynamicProxyFactory create(boolean proxyTargetClass) {
        return !proxyTargetClass ? new JdkDynamicProxyFactory() : new CglibDynamicProxyFactory();
    }

    public abstract Object createProxy(Object source);

    public Object createProxy(Object source, BeanDefinition beanDefinition) {
        return createProxy(source, beanDefinition.getConstructArgTypes(), beanDefinition.getConstructArgValues());
    }

    public abstract Object createProxy(Object source, Class<?>[] argTypes, Object[] arsValues);
}
