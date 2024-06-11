package com.kfyty.loveqq.framework.boot.processor;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.BeanPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.EventListener;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
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
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述: EventListener 注解处理器
 *
 * @author kfyty725
 * @date 2021/6/21 18:02
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Component
@Order(Order.HIGHEST_PRECEDENCE + 200)
public class EventListenerAnnotationBeanPostProcessor implements BeanPostProcessor {
    @Autowired
    private ApplicationContext context;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private EventListenerAnnotationListenerFactory eventListenerAnnotationListenerFactory;

    /**
     * 已经解析的非单例 bean name
     */
    private final Set<String> resolvePrototypeEvent = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        BeanDefinition beanDefinition = this.context.getBeanDefinition(beanName);
        if (this.resolvePrototypeEvent.contains(beanName) || !beanDefinition.isAutowireCandidate()) {
            return null;
        }
        Class<?> beanClass = beanDefinition.getBeanType();
        for (Method method : ReflectUtil.getMethods(beanClass)) {
            if (AnnotationUtil.hasAnnotation(method, EventListener.class)) {
                Method listenerMethod = AopUtil.getInterfaceMethod(bean, method);
                this.createEventListener(beanName, listenerMethod, AnnotationUtil.findAnnotation(method, EventListener.class));
            }
        }
        if (!beanDefinition.isSingleton()) {
            this.resolvePrototypeEvent.add(beanName);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void createEventListener(String beanName, Method listenerMethod, EventListener eventListener) {
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
            log.info("register annotation event listener: {}", annotationListener);
        }
    }
}
