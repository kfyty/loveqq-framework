package com.kfyty.boot.autoconfig.factory;

import com.kfyty.boot.proxy.LookupMethodInterceptorProxy;
import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.core.autoconfig.beans.FactoryBean;
import com.kfyty.core.proxy.factory.DynamicProxyFactory;
import lombok.RequiredArgsConstructor;

/**
 * 描述: 导入存在 Lookup 注解的方法的 bean 的 FactoryBean
 *
 * @author kfyty725
 * @date 2021/7/11 12:43
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class LookupBeanFactoryBean<T> implements ApplicationContextAware, FactoryBean<T> {
    /**
     * bean type
     */
    private final Class<?> beanType;

    /**
     * 应用上下文
     */
    private ApplicationContext applicationContext;

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
        return (T) DynamicProxyFactory
                .create(true)
                .addInterceptorPoint(new LookupMethodInterceptorProxy(this.applicationContext))
                .createProxy(this.getBeanType());
    }
}
