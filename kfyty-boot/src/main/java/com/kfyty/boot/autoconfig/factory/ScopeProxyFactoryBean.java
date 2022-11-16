package com.kfyty.boot.autoconfig.factory;

import com.kfyty.boot.proxy.ScopeProxyInterceptorProxy;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.autoconfig.beans.FactoryBean;
import com.kfyty.core.autoconfig.scope.ScopeProxyFactory;
import com.kfyty.core.proxy.factory.DynamicProxyFactory;
import lombok.NoArgsConstructor;

import static com.kfyty.core.utils.BeanUtil.SCOPE_PROXY_SOURCE_PREFIX;

/**
 * 描述: 为非单例 bean 创建作用域代理
 *
 * @author kfyty725
 * @date 2021/7/11 12:43
 * @email kfyty725@hotmail.com
 */
@NoArgsConstructor
public class ScopeProxyFactoryBean<T> implements FactoryBean<T> {
    /**
     * 作用域代理目标 bean 定义
     */
    private BeanDefinition scopedTarget;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private ScopeProxyFactory scopeProxyFactory;

    @Autowired
    public ScopeProxyFactoryBean(BeanDefinition scopedTarget) {
        this.scopedTarget = scopedTarget;
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
