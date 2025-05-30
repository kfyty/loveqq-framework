package com.kfyty.loveqq.framework.boot.autoconfig.support;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.scope.ScopeProxyFactory;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.event.PropertyConfigRefreshedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述: 刷新作用域代理工厂
 *
 * @author kfyty725
 * @date 2022/10/22 10:19
 * @email kfyty725@hotmail.com
 */
public class RefreshScopeProxyFactory implements ScopeProxyFactory {
    /**
     * bean 缓存
     */
    protected final Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public Object getObject(BeanDefinition beanDefinition, BeanFactory beanFactory) {
        return this.cache.computeIfAbsent(beanDefinition.getBeanName(), beanName -> beanFactory.registerBean(beanDefinition));
    }

    @Override
    public void onApplicationEvent(ApplicationEvent<?> event) {
        if (event instanceof PropertyConfigRefreshedEvent) {
            Map<String, Object> removed = new HashMap<>(this.cache);
            ApplicationContext context = (ApplicationContext) event.getSource();
            this.cache.clear();
            removed.forEach(context::destroyBean);
        }
    }
}
