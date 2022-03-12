package com.kfyty.support.proxy;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.proxy.factory.DynamicProxyFactory;
import com.kfyty.support.utils.AopUtil;
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

    @Override
    public Object postProcessAfterInstantiation(Object bean, String beanName) {
        if (this.canCreateProxy(bean, beanName)) {
            return this.createProxy(bean, beanName, this.createProxyPoint());
        }
        return null;
    }

    public boolean canCreateProxy(Object bean, String beanName) {
        return false;
    }

    public InterceptorChainPoint createProxyPoint() {
        return null;
    }

    public Object createProxy(Object bean, String beanName, InterceptorChainPoint interceptorChainPoint) {
        if (AopUtil.addProxyInterceptorPoint(bean, interceptorChainPoint)) {
            return bean;
        }
        BeanDefinition beanDefinition = this.applicationContext.getBeanDefinition(beanName);
        Object proxy = DynamicProxyFactory.create(bean, this.applicationContext).createProxy(bean, beanDefinition);
        AopUtil.addProxyInterceptorPoint(proxy, interceptorChainPoint);
        if (log.isDebugEnabled()) {
            log.debug("proxy target bean: {} -> {}", bean, proxy);
        }
        return proxy;
    }
}
