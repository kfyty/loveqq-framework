package com.kfyty.boot.processor;

import com.kfyty.boot.proxy.AsyncMethodInterceptorProxy;
import com.kfyty.boot.proxy.BeanMethodInterceptorProxy;
import com.kfyty.boot.proxy.LookupMethodInterceptorProxy;
import com.kfyty.support.autoconfig.annotation.Async;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.Lookup;
import com.kfyty.support.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/13 17:27
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Configuration
public class ConfigurationBeanPostProcessor extends AbstractProxyCreatorProcessor {

    @Override
    public Object postProcessAfterInstantiation(Object bean, String beanName) {
        Class<?> beanClass = AopUtil.getSourceIfNecessary(bean).getClass();
        if(AnnotationUtil.hasAnnotationElement(beanClass, Configuration.class)) {
            bean = this.createProxy(bean, beanName, new BeanMethodInterceptorProxy(this.applicationContext));
        }
        if(AnnotationUtil.hasAnnotation(beanClass, Async.class) || ReflectUtil.getMethods(beanClass).stream().anyMatch(e -> AnnotationUtil.hasAnnotation(e, Async.class))) {
            bean = this.createProxy(bean, beanName, new AsyncMethodInterceptorProxy(this.applicationContext));
        }
        if(!ReflectUtil.isAbstract(this.applicationContext.getBeanDefinition(beanName).getBeanType()) && ReflectUtil.getMethods(beanClass).stream().anyMatch(e -> AnnotationUtil.hasAnnotation(e, Lookup.class))) {
            bean = this.createProxy(bean, beanName, new LookupMethodInterceptorProxy(this.applicationContext));
        }
        return bean;
    }
}
