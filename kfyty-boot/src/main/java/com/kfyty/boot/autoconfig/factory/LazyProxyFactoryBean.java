package com.kfyty.boot.autoconfig.factory;

import com.kfyty.boot.proxy.LazyProxyInterceptorProxy;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.BeanFactory;
import com.kfyty.support.autoconfig.beans.FactoryBean;
import com.kfyty.support.proxy.factory.DynamicProxyFactory;
import lombok.NoArgsConstructor;

import static com.kfyty.support.utils.BeanUtil.LAZY_PROXY_SOURCE_PREFIX;

/**
 * 描述: 为延迟初始化 bean 创建代理
 *
 * @author kfyty725
 * @date 2021/7/11 12:43
 * @email kfyty725@hotmail.com
 */
@NoArgsConstructor
public class LazyProxyFactoryBean<T> implements FactoryBean<T> {
    /**
     * 延迟初始化代理目标 bean 定义
     */
    private BeanDefinition lazedTarget;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    public LazyProxyFactoryBean(BeanDefinition lazedTarget) {
        this.lazedTarget = lazedTarget;
        if (!lazedTarget.getBeanName().startsWith(LAZY_PROXY_SOURCE_PREFIX)) {
            lazedTarget.setBeanName(LAZY_PROXY_SOURCE_PREFIX + lazedTarget.getBeanName());
        }
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
}