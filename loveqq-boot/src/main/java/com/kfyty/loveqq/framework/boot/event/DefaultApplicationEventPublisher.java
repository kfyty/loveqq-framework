package com.kfyty.loveqq.framework.boot.event;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.event.ApplicationEventPublisher;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.event.EventListenerAnnotationListener;
import com.kfyty.loveqq.framework.core.event.GenericApplicationEvent;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;

import java.lang.reflect.ParameterizedType;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    public static final Predicate<ParameterizedType> SUPER_GENERIC_FILTER = type -> type.getRawType().equals(ApplicationListener.class);

    /**
     * 上下文是否刷新完成
     */
    private volatile boolean isRefreshed = false;

    /**
     * 过早发布的事件
     */
    private Queue<ApplicationEvent<?>> earlyPublishedEvent = new ConcurrentLinkedQueue<>();

    /**
     * 注册的事件监听器
     */
    private final Queue<ApplicationListener> applicationListeners = new ConcurrentLinkedQueue<>();

    @Override
    public void onAfterRefreshed(ApplicationContext applicationContext) {
        this.isRefreshed = true;
        if (CommonUtil.notEmpty(this.earlyPublishedEvent)) {
            this.earlyPublishedEvent.forEach(this::publishEvent);
            this.earlyPublishedEvent.clear();
        }
        this.earlyPublishedEvent = null;
    }

    @Override
    public void registerEventListener(ApplicationListener<?> applicationListener) {
        this.applicationListeners.add(applicationListener);
    }

    /**
     * 获取监听器的监听类型时，如果被 jdk 代理，则应使用原对象，否则无法获取泛型信息
     */
    @Override
    public void publishEvent(ApplicationEvent<?> event) {
        if (!this.isRefreshed) {
            this.earlyPublishedEvent.add(event);
            return;
        }
        for (ApplicationListener applicationListener : this.applicationListeners) {
            Class<?> listenerType = null;
            if (applicationListener instanceof EventListenerAnnotationListener) {
                listenerType = ((EventListenerAnnotationListener) applicationListener).getListenerType();
            } else {
                Class<?> listenerClass = AopUtil.getTargetClass(applicationListener);
                listenerType = ReflectUtil.getSuperGeneric(listenerClass, SUPER_GENERIC_FILTER);
            }
            if (event instanceof GenericApplicationEvent<?, ?> && listenerType.equals(((GenericApplicationEvent) event).getEventType())) {
                applicationListener.onApplicationEvent(event);
                continue;
            }
            if (listenerType.equals(ApplicationEvent.class) || listenerType.equals(event.getClass())) {
                applicationListener.onApplicationEvent(event);
            }
        }
    }
}
