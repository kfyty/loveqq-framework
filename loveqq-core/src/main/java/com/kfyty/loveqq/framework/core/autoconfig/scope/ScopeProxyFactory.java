package com.kfyty.loveqq.framework.core.autoconfig.scope;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;

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
     * 执行目标方法后的回调
     * 主要是原型作用域 bean 的销毁处理
     *
     * @param beanDefinition bean 定义
     * @param beanFactory    bean 工厂
     * @param bean           目标 bean
     * @param methodProxy    执行的代理方法
     */
    default void onInvoked(BeanDefinition beanDefinition, BeanFactory beanFactory, Object bean, MethodProxy methodProxy) {

    }

    /**
     * 事件处理
     *
     * @param event 事件
     */
    default void onApplicationEvent(ApplicationEvent<?> event) {

    }
}
