package com.kfyty.core.event;

import java.lang.reflect.Method;

/**
 * 描述: {@link com.kfyty.core.autoconfig.annotation.EventListener} 标记的监听器工厂
 *
 * @author kfyty725
 * @date 2021/6/21 16:43
 * @email kfyty725@hotmail.com
 */
public interface EventListenerAnnotationListenerFactory {
    /**
     * 创建事件监听器
     *
     * @param beanName       bean name
     * @param listenerMethod 监听方法
     * @param listenerType   监听类型
     * @return 监听器
     */
    ApplicationListener<?> createEventListener(String beanName, Method listenerMethod, Class<?> listenerType);
}
