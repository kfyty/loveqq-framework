package com.kfyty.loveqq.framework.boot.autoconfig.factory;

import com.kfyty.loveqq.framework.boot.proxy.LazyProxyInterceptorProxy;
import com.kfyty.loveqq.framework.core.autoconfig.aware.BeanFactoryAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.core.proxy.factory.DynamicProxyFactory;
import lombok.RequiredArgsConstructor;

/**
 * 描述: 为延迟初始化 bean 创建代理
 *
 * @author kfyty725
 * @date 2021/7/11 12:43
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class LazyProxyFactoryBean<T> implements FactoryBean<T>, BeanFactoryAware {
    /**
     * 延迟初始化代理目标 bean 定义
     */
    private final BeanDefinition lazedTarget;

    /**
     * bean 工厂
     */
    private BeanFactory beanFactory;

    public LazyProxyFactoryBean<T> withBeanFactory(BeanFactory beanFactory) {
        this.setBeanFactory(beanFactory);
        return this;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Class<?> getBeanType() {
        return this.lazedTarget.getBeanType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        return (T) DynamicProxyFactory
                .create(true)
                .addInterceptorPoint(new LazyProxyInterceptorProxy(this.lazedTarget.getBeanName(), this.beanFactory))
                .createProxy(this.getBeanType());
    }

    @Override
    public boolean shouldApplyLifecycle() {
        return false;
    }
}
