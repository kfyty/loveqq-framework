package com.kfyty.boot.processor;

import com.kfyty.boot.proxy.BeanMethodInterceptorProxy;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.BootApplication;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.proxy.factory.DynamicProxyFactory;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.BeanUtil;
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
public class ConfigurationBeanProcessor implements ApplicationContextAware, InstantiationAwareBeanPostProcessor {
    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object postProcessAfterInstantiation(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();
        if(!AnnotationUtil.hasAnyAnnotation(beanClass, Configuration.class, BootApplication.class)) {
            return null;
        }
        if(AopUtil.isProxy(bean)) {
            AopUtil.getInterceptorChain(bean).addInterceptorPoint(new BeanMethodInterceptorProxy(this.context));
            return null;
        }
        BeanDefinition beanDefinition = this.context.getBeanDefinition(beanName);
        Object proxy = DynamicProxyFactory.create(bean, this.context).createProxy(bean, beanDefinition);
        AopUtil.getInterceptorChain(proxy).addInterceptorPoint(new BeanMethodInterceptorProxy(this.context));
        if(log.isDebugEnabled()) {
            log.debug("proxy configuration bean: {} -> {}", bean, proxy);
        }
        return BeanUtil.copyBean(bean, proxy);
    }
}
