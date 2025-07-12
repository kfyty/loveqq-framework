package com.kfyty.loveqq.framework.boot.autoconfig.support;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.scope.ScopeProxyFactory;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.exception.BeansException;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 描述: 默认的代理工厂
 *
 * @author kfyty725
 * @date 2022/10/22 10:12
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class DefaultScopeProxyFactory implements ScopeProxyFactory, ApplicationListener<ApplicationEvent<?>> {
    /**
     * 作用域代理工厂
     */
    protected volatile Map<String, ScopeProxyFactory> scopeProxyFactoryMap;

    @Override
    public Object getObject(BeanDefinition beanDefinition, BeanFactory beanFactory) {
        return this.obtainScopeProxyFactory(beanDefinition.getScope(), beanFactory).getObject(beanDefinition, beanFactory);
    }

    @Override
    public void onInvoked(BeanDefinition beanDefinition, BeanFactory beanFactory, Object bean, MethodProxy methodProxy) {
        this.obtainScopeProxyFactory(beanDefinition.getScope(), beanFactory).onInvoked(beanDefinition, beanFactory, bean, methodProxy);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent<?> event) {
        if (this.scopeProxyFactoryMap != null) {
            List<ScopeProxyFactory> copy = new ArrayList<>(this.scopeProxyFactoryMap.values());
            copy.stream().filter(e -> e != this).forEach(v -> v.onApplicationEvent(event));
        }
    }

    protected ScopeProxyFactory obtainScopeProxyFactory(String scope, BeanFactory beanFactory) {
        if (this.scopeProxyFactoryMap == null) {
            synchronized (this) {
                if (this.scopeProxyFactoryMap == null) {
                    this.scopeProxyFactoryMap = beanFactory.getBeanOfType(ScopeProxyFactory.class);
                }
            }
        }
        ScopeProxyFactory scopeProxyFactory = this.scopeProxyFactoryMap.get(scope);
        if (scopeProxyFactory == null) {
            throw new BeansException("This scope doesn't supported yet: " + scope);
        }
        return scopeProxyFactory;
    }
}
