package com.kfyty.loveqq.framework.boot.autoconfig.support;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.scope.ScopeProxyFactory;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.exception.BeansException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
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
    protected volatile Map<String, ScopeProxyFactory> scopeProxyFactoryMap;

    @Override
    public Object getObject(BeanDefinition beanDefinition, BeanFactory beanFactory) {
        this.ensureScopeProxyFactory(beanFactory);
        return this.obtainScopeProxyFactory(beanDefinition.getScope()).getObject(beanDefinition, beanFactory);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent<?> event) {
        if (this.scopeProxyFactoryMap != null) {
            Map<String, ScopeProxyFactory> copy = new HashMap<>(this.scopeProxyFactoryMap);
            copy.values().stream().filter(e -> e != this).forEach(v -> v.onApplicationEvent(event));
        }
    }

    protected void ensureScopeProxyFactory(BeanFactory beanFactory) {
        if (this.scopeProxyFactoryMap == null) {
            synchronized (this) {
                if (this.scopeProxyFactoryMap == null) {
                    this.scopeProxyFactoryMap = beanFactory.getBeanOfType(ScopeProxyFactory.class);
                }
            }
        }
    }

    protected ScopeProxyFactory obtainScopeProxyFactory(String scope) {
        ScopeProxyFactory scopeProxyFactory = this.scopeProxyFactoryMap.get(scope);
        if (scopeProxyFactory == null) {
            throw new BeansException("this scope is not supported temporarily: " + scope);
        }
        return scopeProxyFactory;
    }
}
