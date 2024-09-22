package com.kfyty.loveqq.framework.boot.event;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.EventListener;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.event.ApplicationEventPublisher;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.event.EventListenerAnnotationListenerFactory;
import com.kfyty.loveqq.framework.core.event.GenericApplicationEvent;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import static com.kfyty.loveqq.framework.boot.event.DefaultApplicationEventPublisher.SUPER_GENERIC_FILTER;

/**
 * 描述: 事件监听器处理器
 *
 * @author kfyty725
 * @date 2022/10/23 15:30
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Component
public class EventListenerResolver implements ContextAfterRefreshed, InternalPriority {
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private EventListenerAnnotationListenerFactory eventListenerAnnotationListenerFactory;

    @Override
    public void onAfterRefreshed(ApplicationContext applicationContext) {
        this.registerApplicationListener(applicationContext);
        this.registerEventListenerAnnotation(applicationContext);
    }

    protected void registerApplicationListener(ApplicationContext applicationContext) {
        Map<String, BeanDefinition> beanDefinitions = applicationContext.getBeanDefinitions(ApplicationListener.class, true);
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitions.entrySet()) {
            if (entry.getValue().isSingleton()) {
                this.applicationEventPublisher.registerEventListener(applicationContext.getBean(entry.getKey()));
            } else {
                Class<?> listenerType = ReflectUtil.getSuperGeneric(entry.getValue().getBeanType(), SUPER_GENERIC_FILTER);
                ApplicationListener<?> eventListener = this.eventListenerAnnotationListenerFactory.createEventListener(entry.getKey(), null, listenerType);
                this.applicationEventPublisher.registerEventListener(eventListener);
            }
        }
    }

    protected void registerEventListenerAnnotation(ApplicationContext applicationContext) {
        Map<String, BeanDefinition> beanDefinitionMap = applicationContext.getBeanDefinitionWithAnnotation(EventListener.class, true);
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            Class<?> beanType = entry.getValue().getBeanType();
            for (Method method : ReflectUtil.getMethods(beanType)) {
                EventListener annotation = AnnotationUtil.findAnnotation(method, EventListener.class);
                if (annotation != null) {
                    Method listenerMethod = AopUtil.getInterfaceMethod(beanType, method);
                    this.createEventListener(entry.getKey(), listenerMethod, annotation);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void createEventListener(String beanName, Method listenerMethod, EventListener eventListener) {
        Class<? extends ApplicationEvent<?>>[] eventTypes = eventListener.value();
        if (CommonUtil.empty(eventTypes)) {
            eventTypes = (Class<? extends ApplicationEvent<?>>[]) listenerMethod.getParameterTypes();
        }
        Type[] parameterTypes = listenerMethod.getGenericParameterTypes();
        for (int i = 0; i < eventTypes.length; i++) {
            Class<?> eventType = eventTypes[i];
            if (GenericApplicationEvent.class.isAssignableFrom(eventType)) {
                eventType = ReflectUtil.getActualGenericType("T", parameterTypes[i]);
            }
            ApplicationListener<?> annotationListener = this.eventListenerAnnotationListenerFactory.createEventListener(beanName, listenerMethod, eventType);
            this.applicationEventPublisher.registerEventListener(annotationListener);
            log.info("Register annotation event listener: {}", annotationListener);
        }
    }
}
