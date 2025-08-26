package com.kfyty.loveqq.framework.boot.event;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ContextOnRefresh;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.EventListener;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.event.ApplicationEventPublisher;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.event.EventListenerAnnotationListener;
import com.kfyty.loveqq.framework.core.event.GenericApplicationEvent;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import static com.kfyty.loveqq.framework.boot.event.DefaultApplicationEventPublisher.GENERIC_EVENT_GENERIC_FILTER;
import static com.kfyty.loveqq.framework.boot.event.DefaultApplicationEventPublisher.GENERIC_EVENT_GENERIC_MAPPING;
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
public class EventListenerRegistry implements ContextOnRefresh, InternalPriority {
    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void onRefresh(ApplicationContext applicationContext) {
        this.registerApplicationListener(applicationContext);
        this.registerEventAnnotationListener(applicationContext);
    }

    /**
     * 注册 {@link ApplicationListener} 实例的监听器
     *
     * @param applicationContext 应用上下文
     */
    protected void registerApplicationListener(ApplicationContext applicationContext) {
        Map<String, BeanDefinition> beanDefinitions = applicationContext.getBeanDefinitions(ApplicationListener.class, true);
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitions.entrySet()) {
            if (entry.getValue().isSingleton()) {
                this.applicationEventPublisher.registerEventListener(applicationContext.getBean(entry.getKey()));
            } else {
                Class<?> listenerClass = entry.getValue().getBeanType();
                Class<?> listenerType = ReflectUtil.getSuperGeneric(listenerClass, SUPER_GENERIC_FILTER);
                if (GenericApplicationEvent.class.isAssignableFrom(listenerType)) {
                    listenerType = ReflectUtil.getSuperGeneric(listenerType, GENERIC_EVENT_GENERIC_MAPPING.apply(listenerClass), 0, GENERIC_EVENT_GENERIC_FILTER);
                }
                this.applicationEventPublisher.registerEventListener(this.createEventListener(entry.getKey(), null, listenerType, null, applicationContext));
            }
            log.info("Register event listener: {}", entry.getValue().getBeanType());
        }
    }

    /**
     * 注册 {@link EventListener} 注解的监听器
     *
     * @param applicationContext 应用上下文
     */
    protected void registerEventAnnotationListener(ApplicationContext applicationContext) {
        Map<String, BeanDefinition> beanDefinitionMap = applicationContext.getBeanDefinitionWithAnnotation(EventListener.class, true);
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            Class<?> beanType = entry.getValue().getBeanType();
            for (Method method : ReflectUtil.getMethods(beanType)) {
                EventListener annotation = AnnotationUtil.findAnnotation(method, EventListener.class);
                if (annotation != null) {
                    Method listenerMethod = AopUtil.getInterfaceMethod(beanType, method);
                    this.registerEventAnnotationListener(entry.getKey(), listenerMethod, annotation, applicationContext);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void registerEventAnnotationListener(String beanName, Method listenerMethod, EventListener eventListener, ApplicationContext applicationContext) {
        Class<? extends ApplicationEvent<?>>[] eventTypes = eventListener.value();
        if (CommonUtil.empty(eventTypes)) {
            eventTypes = (Class<? extends ApplicationEvent<?>>[]) listenerMethod.getParameterTypes();
        }
        Type[] parameterTypes = listenerMethod.getGenericParameterTypes();
        for (int i = 0; i < eventTypes.length; i++) {
            Class<?> eventType = eventTypes[i];
            if (GenericApplicationEvent.class.isAssignableFrom(eventType)) {
                eventType = ReflectUtil.getSuperGeneric(eventType, parameterTypes[i], 0, GENERIC_EVENT_GENERIC_FILTER);
            }
            ApplicationListener<?> annotationListener = this.createEventListener(beanName, listenerMethod, eventType, eventListener.condition(), applicationContext);
            this.applicationEventPublisher.registerEventListener(annotationListener);
            log.info("Register annotation event listener: {}", annotationListener);
        }
    }

    /**
     * 创建事件监听器
     *
     * @param beanName           bean name
     * @param listenerMethod     监听方法
     * @param listenerType       监听类型
     * @param condition          监听条件
     * @param applicationContext 应用上下文
     * @return 监听器
     */
    protected ApplicationListener<?> createEventListener(String beanName, Method listenerMethod, Class<?> listenerType, String condition, ApplicationContext applicationContext) {
        return new EventListenerAnnotationListener(beanName, listenerMethod, listenerType, condition, applicationContext);
    }
}
