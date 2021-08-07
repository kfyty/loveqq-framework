package com.kfyty.support.proxy;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.proxy.factory.DynamicProxyFactory;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.BeanUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 描述: 创建代理
 *
 * @author kfyty725
 * @date 2021/6/27 11:00
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class AbstractProxyCreatorProcessor implements ApplicationContextAware, InstantiationAwareBeanPostProcessor {
    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Object createProxy(Object bean, String beanName, InterceptorChainPoint interceptorChainPoint) {
        if (AopUtil.addProxyInterceptorPoint(bean, interceptorChainPoint)) {
            return bean;
        }
        BeanDefinition beanDefinition = this.applicationContext.getBeanDefinition(beanName);
        Object proxy = DynamicProxyFactory.create(bean, this.applicationContext).createProxy(bean, beanDefinition);
        AopUtil.addProxyInterceptorPoint(proxy, interceptorChainPoint);
        if (AnnotationUtil.hasAnnotationElement(bean, Configuration.class)) {
            BeanUtil.copyProperties(bean, proxy);
        }
        if (log.isDebugEnabled()) {
            log.debug("proxy target bean: {} -> {}", bean, proxy);
        }
        return proxy;
    }
}
