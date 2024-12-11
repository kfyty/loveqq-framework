package com.kfyty.loveqq.framework.boot.event;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.event.ApplicationEventPublisher;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.event.EventListenerAdapter;
import com.kfyty.loveqq.framework.core.event.EventListenerAnnotationListener;
import com.kfyty.loveqq.framework.core.event.GenericApplicationEvent;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
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
    private volatile Queue<ApplicationEvent<?>> earlyPublishedEvent = new ConcurrentLinkedQueue<>();

    /**
     * 事件适配器，需要懒加载
     */
    private volatile Collection<EventListenerAdapter> eventListenerAdapters;

    /**
     * 注册的事件监听器
     */
    private final Queue<Pair<Class<?>, ApplicationListener>> applicationListeners = new ConcurrentLinkedQueue<>();

    @Override
    public void onAfterRefreshed(ApplicationContext applicationContext) {
        this.isRefreshed = true;
        this.ensureEventListenerAdapter(applicationContext);
        if (CommonUtil.notEmpty(this.earlyPublishedEvent)) {
            this.earlyPublishedEvent.forEach(this::publishEvent);
            this.earlyPublishedEvent.clear();
        }
        this.earlyPublishedEvent = null;
    }

    @Override
    public void registerEventListener(ApplicationListener<?> applicationListener) {
        final Class<?> listenerType;
        if (applicationListener instanceof EventListenerAnnotationListener) {
            listenerType = ((EventListenerAnnotationListener) applicationListener).getListenerType();
        } else {
            Class<?> listenerClass = AopUtil.getTargetClass(applicationListener);
            listenerType = ReflectUtil.getSuperGeneric(listenerClass, SUPER_GENERIC_FILTER);
        }
        this.applicationListeners.add(new Pair<>(listenerType, applicationListener));
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
        for (Pair<Class<?>, ApplicationListener> applicationListenerPair : this.applicationListeners) {
            Class<?> listenerType = applicationListenerPair.getKey();
            ApplicationListener applicationListener = applicationListenerPair.getValue();
            if (event instanceof GenericApplicationEvent<?, ?> && listenerType == ((GenericApplicationEvent) event).getEventType()) {
                this.adaptEventListener(applicationListener).onApplicationEvent(event);
                continue;
            }
            if (listenerType == ApplicationEvent.class || listenerType == event.getClass()) {
                this.adaptEventListener(applicationListener).onApplicationEvent(event);
                continue;
            }
        }
    }

    /**
     * 适配事件监听器
     *
     * @param listener 监听器
     * @return 适配后的监听器
     */
    @SuppressWarnings("unchecked")
    protected ApplicationListener adaptEventListener(ApplicationListener listener) {
        if (this.eventListenerAdapters == null || this.eventListenerAdapters.isEmpty()) {
            return listener;
        }
        final ApplicationListener source = listener;
        for (EventListenerAdapter listenerAdapter : this.eventListenerAdapters) {
            listener = listenerAdapter.adapt(source, listener);
        }
        return listener;
    }

    /**
     * 准备事件适配器
     */
    protected void ensureEventListenerAdapter(ApplicationContext applicationContext) {
        if (this.eventListenerAdapters == null) {
            synchronized (this) {
                if (this.eventListenerAdapters == null) {
                    this.eventListenerAdapters = applicationContext.getBeanOfType(EventListenerAdapter.class).values();
                }
            }
        }
    }
}
