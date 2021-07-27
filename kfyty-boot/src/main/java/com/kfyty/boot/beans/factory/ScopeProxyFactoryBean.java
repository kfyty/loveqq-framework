package com.kfyty.boot.beans.factory;

import com.kfyty.boot.proxy.ScopeProxyInterceptorProxy;
import com.kfyty.support.autoconfig.BeanFactoryAware;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.BeanFactory;
import com.kfyty.support.autoconfig.beans.FactoryBean;
import com.kfyty.support.proxy.InterceptorChain;
import com.kfyty.support.utils.ReflectUtil;
import net.sf.cglib.proxy.Enhancer;

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
    public static final String SCOPE_PROXY_SOURCE_PREFIX = "scope.proxy.source.";

    private BeanFactory beanFactory;

    private final String beanName;
    private final BeanDefinition sourceBeanDefinition;

    public ScopeProxyFactoryBean(BeanDefinition sourceBeanDefinition) {
        this.beanName = sourceBeanDefinition.getBeanName();
        this.sourceBeanDefinition = sourceBeanDefinition;
        if (!this.beanName.startsWith(SCOPE_PROXY_SOURCE_PREFIX)) {
            ReflectUtil.setFinalFieldValue(sourceBeanDefinition, ReflectUtil.getField(sourceBeanDefinition.getClass(), "beanName"), SCOPE_PROXY_SOURCE_PREFIX + sourceBeanDefinition.getBeanName());
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public String getBeanName() {
        return this.beanName;
    }

    @Override
    public Class<?> getBeanType() {
        return this.sourceBeanDefinition.getBeanType();
    }

    @Override
    public T getObject() {
        Enhancer enhancer = new Enhancer();
        InterceptorChain interceptorChain = new InterceptorChain(null);
        enhancer.setSuperclass(this.getBeanType());
        enhancer.setCallback(interceptorChain.addInterceptorPoint(new ScopeProxyInterceptorProxy(this.beanFactory, this.sourceBeanDefinition)));
        return (T) enhancer.create();
    }
}
