package com.kfyty.boot.proxy;

import com.kfyty.support.autoconfig.ConfigurableContext;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.utils.BeanUtil;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * 描述: Configuration 注解代理
 *
 * @author kfyty725
 * @date 2021/6/13 17:30
 * @email kfyty725@hotmail.com
 */
public class ConfigurationAnnotationEnhancerProxy implements MethodInterceptor {
    private final ConfigurableContext context;
    private final Set<String> proxying;

    public ConfigurationAnnotationEnhancerProxy(ConfigurableContext context) {
        this.context = context;
        this.proxying = new HashSet<>();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Bean annotation = method.getAnnotation(Bean.class);
        if(annotation == null) {
            return methodProxy.invokeSuper(o, objects);
        }
        String beanName = BeanUtil.getBeanName(method.getReturnType(), annotation);
        Object bean = this.context.getBean(beanName);
        if(bean != null) {
            return bean;
        }
        BeanDefinition beanDefinition = this.context.getBeanDefinition(beanName, method.getReturnType());
        if(beanDefinition == null || this.proxying.contains(beanDefinition.getBeanName())) {
            return methodProxy.invokeSuper(o, objects);
        }
        this.proxying.add(beanDefinition.getBeanName());
        bean = this.context.registerBean(beanDefinition);
        this.proxying.remove(beanDefinition.getBeanName());
        return bean;
    }
}
