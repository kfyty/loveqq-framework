package com.kfyty.boot.event;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ContextAfterRefreshed;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.Lazy;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.event.ApplicationEvent;
import com.kfyty.support.event.ApplicationEventPublisher;
import com.kfyty.support.event.ApplicationListener;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 描述: 事件发布器默认实现
 *
 * @author kfyty725
 * @date 2021/6/21 16:56
 * @email kfyty725@hotmail.com
 */
@Order(Order.HIGHEST_PRECEDENCE)
@Component("applicationEventPublisher")
@SuppressWarnings({"unchecked", "rawtypes"})
public class DefaultApplicationEventPublisher implements ContextAfterRefreshed, ApplicationEventPublisher {
    /**
     * 父类泛型过滤器
     */
    private static final Predicate<Type> SUPER_GENERIC_FILTER = type -> type instanceof ParameterizedType && ((ParameterizedType) type).getRawType().equals(ApplicationListener.class);

    /**
     * 上下文是否刷新完成
     */
    private boolean isRefreshed = false;

    /**
     * 注册的事件监听器
     */
    private final List<ApplicationListener> applicationListeners = new ArrayList<>();

    /**
     * 过早发布的事件
     */
    private final List<ApplicationEvent<?>> earlyPublishedEvent = new LinkedList<>();

    @Lazy
    @Autowired(required = false)
    public void setApplicationListeners(List<ApplicationListener> applicationListeners) {
        applicationListeners.forEach(this::registerEventListener);
    }

    @Override
    public void onAfterRefreshed(ApplicationContext applicationContext) {
        this.isRefreshed = true;
        if (CommonUtil.notEmpty(this.earlyPublishedEvent)) {
            this.earlyPublishedEvent.forEach(this::publishEvent);
            this.earlyPublishedEvent.clear();
        }
    }

    /**
     * 获取监听器的监听类型时，如果被 jdk 代理，则应使用原对象，否则无法获取泛型信息
     */
    @Override
    public void publishEvent(ApplicationEvent<?> event) {
        if (!isRefreshed) {
            this.earlyPublishedEvent.add(event);
            return;
        }
        for (ApplicationListener applicationListener : this.applicationListeners) {
            Class<?> listenerType = null;
            Class<?> listenerClass = AopUtil.getTargetClass(applicationListener);
            if (applicationListener instanceof EventListenerAnnotationListener) {
                listenerType = ((EventListenerAnnotationListener) applicationListener).getListenerType();
            } else {
                listenerType = ReflectUtil.getSuperGeneric(listenerClass, SUPER_GENERIC_FILTER);
            }
            if (listenerType.equals(Object.class) || listenerType.equals(event.getClass())) {
                applicationListener.onApplicationEvent(event);
            }
        }
    }

    @Override
    public void registerEventListener(ApplicationListener<?> applicationListener) {
        this.applicationListeners.add(applicationListener);
    }
}
