package com.kfyty.boot.processor;

import com.kfyty.boot.event.EventListenerAnnotationListener;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.BeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.EventListener;
import com.kfyty.support.event.ApplicationEvent;
import com.kfyty.support.event.ApplicationEventPublisher;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;

import java.lang.reflect.Method;

/**
 * 描述: EventListener 注解处理器
 *
 * @author kfyty725
 * @date 2021/6/21 18:02
 * @email kfyty725@hotmail.com
 */
@Configuration
public class EventListenerAnnotationBeanPostProcessor implements BeanPostProcessor {
    @Autowired
    private ApplicationContext context;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 解析 EventListener 注解时，不应获取原对象
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        for (Method method : bean.getClass().getMethods()) {
            if(AnnotationUtil.hasAnnotation(method, EventListener.class)) {
                this.createEventListener(beanName, method, AnnotationUtil.findAnnotation(method, EventListener.class));
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void createEventListener(String beanName, Method listenerMethod, EventListener eventListener) {
        Class<? extends ApplicationEvent<?>>[] eventTypes = eventListener.value();
        if(CommonUtil.empty(eventTypes)) {
            eventTypes = (Class<? extends ApplicationEvent<?>>[]) listenerMethod.getParameterTypes();
        }
        for (Class<? extends ApplicationEvent<?>> eventType : eventTypes) {
            this.applicationEventPublisher.registerEventListener(new EventListenerAnnotationListener(beanName, listenerMethod, eventType, this.context));
        }
    }
}
