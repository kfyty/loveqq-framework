package com.kfyty.loveqq.framework.boot.autoconfig.support;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext;
import com.kfyty.loveqq.framework.core.autoconfig.scope.ScopeProxyFactory;
import com.kfyty.loveqq.framework.core.autoconfig.scope.ScopeRefreshed;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.event.PropertyContextRefreshedEvent;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述: 刷新作用域代理工厂
 *
 * @author kfyty725
 * @date 2022/10/22 10:19
 * @email kfyty725@hotmail.com
 */
@Slf4j
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
        if (event instanceof PropertyContextRefreshedEvent) {
            Map<String, Object> cached = CommonUtil.sortBeanOrder(this.cache);
            PropertyContext propertyContext = ((PropertyContextRefreshedEvent) event).getSource();
            ApplicationContext applicationContext = propertyContext.getApplicationContext();
            for (Map.Entry<String, Object> entry : cached.entrySet()) {
                if (entry.getValue() instanceof ScopeRefreshed) {
                    ((ScopeRefreshed) entry.getValue()).onRefreshed(propertyContext);
                } else {
                    this.cache.remove(entry.getKey());
                    try {
                        applicationContext.destroyBean(entry.getKey(), entry.getValue());
                    } catch (Throwable e) {
                        log.error("Refresh bean error: {}", entry.getKey(), e);
                    }
                }
            }
        }
    }
}
