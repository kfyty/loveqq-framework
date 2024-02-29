package com.kfyty.core.proxy;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.core.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.proxy.factory.DynamicProxyFactory;
import com.kfyty.core.utils.AopUtil;
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
        BeanDefinition beanDefinition = this.getBeanDefinition(beanName);
        if (!beanDefinition.isAutowireCandidate()) {
            return null;
        }
        if (this.canCreateProxy(beanName, beanDefinition.getBeanType(), bean)) {
            return this.createProxy(bean, beanName, this.createProxyPoint());
        }
        return null;
    }

    public BeanDefinition getBeanDefinition(String beanName) {
        return this.applicationContext.getBeanDefinition(beanName);
    }

    public boolean canCreateProxy(String beanName, Class<?> beanType, Object bean) {
        return false;
    }

    public MethodInterceptorChainPoint createProxyPoint() {
        return null;
    }

    public Object createProxy(Object bean, String beanName, MethodInterceptorChainPoint methodInterceptorChainPoint) {
        if (AopUtil.addProxyInterceptorPoint(bean, methodInterceptorChainPoint)) {
            return bean;
        }
        Object proxy = DynamicProxyFactory
                .create(bean, this.applicationContext)
                .addInterceptorPoint(methodInterceptorChainPoint)
                .createProxy(bean, this.getBeanDefinition(beanName));
        if (log.isDebugEnabled()) {
            log.debug("proxy target bean: {} -> {}", bean, proxy);
        }
        return proxy;
    }
}
