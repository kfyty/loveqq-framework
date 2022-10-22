package com.kfyty.boot.autoconfig.factory;

import com.kfyty.boot.proxy.ScopeProxyInterceptorProxy;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.BeanFactory;
import com.kfyty.support.autoconfig.beans.FactoryBean;
import com.kfyty.support.autoconfig.beans.ScopeProxyFactory;
import com.kfyty.support.proxy.factory.DynamicProxyFactory;

/**
 * 描述: 为非单例 bean 创建作用域代理
 *
 * @author kfyty725
 * @date 2021/7/11 12:43
 * @email kfyty725@hotmail.com
 */
public class ScopeProxyFactoryBean<T> implements FactoryBean<T> {
    /**
     * 作用域代理原 bean 名称前缀
     */
    public static final String SCOPE_PROXY_SOURCE_PREFIX = "scopedTarget.";

    /**
     * 作用域代理目标 bean 定义
     */
    private final BeanDefinition scopedTarget;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private ScopeProxyFactory scopeProxyFactory;

    public ScopeProxyFactoryBean(BeanDefinition scopedTarget) {
        this.scopedTarget = scopedTarget;
        if (!scopedTarget.getBeanName().startsWith(SCOPE_PROXY_SOURCE_PREFIX)) {
            scopedTarget.setBeanName(SCOPE_PROXY_SOURCE_PREFIX + scopedTarget.getBeanName());
        }
    }

    @Override
    public Class<?> getBeanType() {
        return this.scopedTarget.getBeanType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        return (T) DynamicProxyFactory
                .create(true)
                .addInterceptorPoint(new ScopeProxyInterceptorProxy(this.scopedTarget, this.beanFactory, this.scopeProxyFactory))
                .createProxy(this.getBeanType());
    }
}
