package com.kfyty.boot.autoconfig.factory;

import com.kfyty.boot.proxy.LookupMethodInterceptorProxy;
import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.beans.FactoryBean;
import com.kfyty.core.proxy.factory.DynamicProxyFactory;
import com.kfyty.core.utils.ScopeUtil;
import lombok.NoArgsConstructor;

/**
 * 描述: 导入存在 Lookup 注解的方法的 bean 的 FactoryBean
 *
 * @author kfyty725
 * @date 2021/7/11 12:43
 * @email kfyty725@hotmail.com
 */
@NoArgsConstructor
public class LookupBeanFactoryBean<T> implements ApplicationContextAware, FactoryBean<T> {
    private Class<?> beanType;
    private String beanScope;
    private ApplicationContext applicationContext;

    @Autowired
    public LookupBeanFactoryBean(Class<?> beanType) {
        this.beanType = beanType;
        this.beanScope = ScopeUtil.resolveScope(beanType).value();
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
        return (T) DynamicProxyFactory
                .create(true)
                .addInterceptorPoint(new LookupMethodInterceptorProxy(this.applicationContext))
                .createProxy(this.getBeanType());
    }

    @Override
    public String getScope() {
        return this.beanScope;
    }
}
