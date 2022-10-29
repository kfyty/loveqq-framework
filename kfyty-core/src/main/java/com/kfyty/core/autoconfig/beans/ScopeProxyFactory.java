package com.kfyty.core.autoconfig.beans;

import com.kfyty.core.event.ApplicationEvent;

/**
 * 描述: 作用域代理工厂
 *
 * @author kfyty725
 * @date 2022/10/22 9:39
 * @email kfyty725@hotmail.com
 */
public interface ScopeProxyFactory {
    /**
     * 根据作用域获取 bean
     *
     * @param beanDefinition bean 定义
     * @param beanFactory    bean 工厂
     * @return bean
     */
    Object getObject(BeanDefinition beanDefinition, BeanFactory beanFactory);

    /**
     * 事件处理
     *
     * @param event 事件
     */
    default void onApplicationEvent(ApplicationEvent<?> event) {

    }
}
