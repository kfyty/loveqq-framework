package com.kfyty.loveqq.framework.boot.autoconfig.factory;

import com.kfyty.loveqq.framework.boot.proxy.ScopeProxyInterceptorProxy;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.core.autoconfig.scope.ScopeProxyFactory;
import com.kfyty.loveqq.framework.core.proxy.factory.DynamicProxyFactory;
import lombok.RequiredArgsConstructor;

/**
 * 描述: 为非单例 bean 创建作用域代理
 *
 * @author kfyty725
 * @date 2021/7/11 12:43
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class ScopeProxyFactoryBean<T> implements FactoryBean<T> {
    /**
     * 作用域代理目标 bean 定义
     */
    private final BeanDefinition scopedTarget;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private ScopeProxyFactory scopeProxyFactory;

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
