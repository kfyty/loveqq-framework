package com.kfyty.boot.autoconfig.factory;

import com.kfyty.boot.proxy.ScopeProxyInterceptorProxy;
import com.kfyty.support.autoconfig.BeanFactoryAware;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.BeanFactory;
import com.kfyty.support.autoconfig.beans.FactoryBean;
import com.kfyty.support.proxy.factory.DynamicProxyFactory;

/**
 * 描述: 为非单例 bean 创建作用域代理
 *
 * @author kfyty725
 * @date 2021/7/11 12:43
 * @email kfyty725@hotmail.com
 */
public class ScopeProxyFactoryBean<T> implements BeanFactoryAware, FactoryBean<T> {
    /**
     * 作用域代理原 bean 名称前缀
     */
    public static final String SCOPE_PROXY_SOURCE_PREFIX = "scopedTarget.";

    private BeanFactory beanFactory;

    private final BeanDefinition sourceBeanDefinition;

    public ScopeProxyFactoryBean(BeanDefinition sourceBeanDefinition) {
        this.sourceBeanDefinition = sourceBeanDefinition;
        if (!sourceBeanDefinition.getBeanName().startsWith(SCOPE_PROXY_SOURCE_PREFIX)) {
            sourceBeanDefinition.setBeanName(SCOPE_PROXY_SOURCE_PREFIX + sourceBeanDefinition.getBeanName());
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Class<?> getBeanType() {
        return this.sourceBeanDefinition.getBeanType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        return (T) DynamicProxyFactory
                .create(true)
                .addInterceptorPoint(new ScopeProxyInterceptorProxy(this.beanFactory, this.sourceBeanDefinition))
                .createProxy(this.getBeanType());
    }
}
