package com.kfyty.boot.processor;

import com.kfyty.boot.proxy.AsyncMethodInterceptorProxy;
import com.kfyty.boot.proxy.BeanMethodInterceptorProxy;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.Async;
import com.kfyty.support.autoconfig.annotation.BootApplication;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.proxy.factory.DynamicProxyFactory;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.BeanUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/13 17:27
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Configuration
public class ConfigurationBeanProcessor implements ApplicationContextAware, InstantiationAwareBeanPostProcessor {
    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object postProcessAfterInstantiation(Object bean, String beanName) {
        Class<?> beanClass = AopUtil.getSourceIfNecessary(bean).getClass();
        if(AnnotationUtil.hasAnyAnnotation(beanClass, Configuration.class, BootApplication.class)) {
            bean = this.doProcessProxy(bean, beanName, new BeanMethodInterceptorProxy(this.context));
        }
        if(AnnotationUtil.hasAnnotation(beanClass, Async.class) || Arrays.stream(beanClass.getMethods()).anyMatch(e -> AnnotationUtil.hasAnnotation(e, Async.class))) {
            bean = this.doProcessProxy(bean, beanName, new AsyncMethodInterceptorProxy(this.context));
        }
        return bean;
    }

    private Object doProcessProxy(Object bean, String beanName, InterceptorChainPoint interceptorChainPoint) {
        if(AopUtil.isProxy(bean)) {
            AopUtil.getInterceptorChain(bean).addInterceptorPoint(interceptorChainPoint);
            return bean;
        }
        BeanDefinition beanDefinition = this.context.getBeanDefinition(beanName);
        Object proxy = DynamicProxyFactory.create(bean, this.context).createProxy(bean, beanDefinition);
        AopUtil.getInterceptorChain(proxy).addInterceptorPoint(interceptorChainPoint);
        if(log.isDebugEnabled()) {
            log.debug("proxy target bean: {} -> {}", bean, proxy);
        }
        return BeanUtil.copyBean(bean, proxy);
    }
}
