package com.kfyty.loveqq.framework.core.proxy;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.proxy.factory.DynamicProxyFactory;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.LogUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 描述: 创建代理
 *
 * @author kfyty725
 * @date 2021/6/27 11:00
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class AbstractProxyCreatorProcessor implements InstantiationAwareBeanPostProcessor, ApplicationContextAware {
    /**
     * {@link ApplicationContext}
     */
    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInstantiation(Object bean, String beanName, BeanDefinition beanDefinition) {
        if (!beanDefinition.isAutowireCandidate()) {
            return null;
        }
        if (this.canCreateProxy(beanName, beanDefinition.getBeanType(), bean)) {
            return this.createProxy(bean, beanDefinition, this.createProxyPoint());
        }
        return null;
    }

    public boolean canCreateProxy(String beanName, Class<?> beanType, Object bean) {
        return false;
    }

    public Object createProxy(Object bean, BeanDefinition beanDefinition, MethodInterceptorChainPoint methodInterceptorChainPoint) {
        if (AopUtil.addProxyInterceptorPoint(bean, methodInterceptorChainPoint)) {
            return bean;
        }
        Object proxy = DynamicProxyFactory
                .create(bean, this.applicationContext)
                .addInterceptorPoint(methodInterceptorChainPoint)
                .createProxy(bean, beanDefinition);
        return LogUtil.logIfDebugEnabled(log, log -> log.debug("Proxy target bean: {} -> {}", bean, proxy), proxy);
    }

    public abstract MethodInterceptorChainPoint createProxyPoint();
}
