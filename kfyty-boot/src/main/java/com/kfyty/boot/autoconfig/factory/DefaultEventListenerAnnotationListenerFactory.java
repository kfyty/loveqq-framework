package com.kfyty.boot.autoconfig.factory;

import com.kfyty.core.event.EventListenerAnnotationListener;
import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.core.event.ApplicationListener;
import com.kfyty.core.event.EventListenerAnnotationListenerFactory;

import java.lang.reflect.Method;

/**
 * 描述: 默认的事件注解监听器工厂
 *
 * @author kfyty725
 * @date 2021/6/21 16:43
 * @email kfyty725@hotmail.com
 */
@Component
@ConditionalOnMissingBean(EventListenerAnnotationListenerFactory.class)
public class DefaultEventListenerAnnotationListenerFactory implements EventListenerAnnotationListenerFactory, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public ApplicationListener<?> createEventListener(String beanName, Method listenerMethod, Class<?> listenerType) {
        return new EventListenerAnnotationListener(beanName, listenerMethod, listenerType, this.applicationContext);
    }
}
