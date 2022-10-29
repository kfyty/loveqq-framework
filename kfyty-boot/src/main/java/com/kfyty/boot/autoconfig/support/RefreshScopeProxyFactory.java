package com.kfyty.boot.autoconfig.support;

import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.autoconfig.beans.ScopeProxyFactory;
import com.kfyty.core.event.ApplicationEvent;
import com.kfyty.core.event.ContextRefreshedEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/10/22 10:19
 * @email kfyty725@hotmail.com
 */
public class RefreshScopeProxyFactory implements ScopeProxyFactory {
    protected Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public Object getObject(BeanDefinition beanDefinition, BeanFactory beanFactory) {
        return this.cache.computeIfAbsent(beanDefinition.getBeanName(), beanName -> beanFactory.registerBean(beanDefinition));
    }

    @Override
    public void onApplicationEvent(ApplicationEvent<?> event) {
        if (event instanceof ContextRefreshedEvent) {
            this.cache.clear();
        }
    }
}
