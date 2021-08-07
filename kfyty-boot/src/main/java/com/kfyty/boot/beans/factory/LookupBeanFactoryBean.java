package com.kfyty.boot.beans.factory;

import com.kfyty.boot.proxy.LookupMethodInterceptorProxy;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.beans.FactoryBean;
import com.kfyty.support.proxy.factory.DynamicProxyFactory;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.ScopeUtil;

/**
 * 描述: 导入存在 Lookup 注解的方法的 bean 的 FactoryBean
 *
 * @author kfyty725
 * @date 2021/7/11 12:43
 * @email kfyty725@hotmail.com
 */
public class LookupBeanFactoryBean<T> implements ApplicationContextAware, FactoryBean<T> {
    private final Class<?> beanType;
    private final boolean isSingleton;

    private ApplicationContext applicationContext;

    public LookupBeanFactoryBean(Class<?> beanType) {
        this.beanType = beanType;
        this.isSingleton = ScopeUtil.isSingleton(beanType);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Class<?> getBeanType() {
        return this.beanType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        Object proxy = DynamicProxyFactory.create(true).createProxy(this.getBeanType());
        AopUtil.addProxyInterceptorPoint(proxy, new LookupMethodInterceptorProxy(this.applicationContext));
        return (T) proxy;
    }

    @Override
    public boolean isSingleton() {
        return this.isSingleton;
    }
}
