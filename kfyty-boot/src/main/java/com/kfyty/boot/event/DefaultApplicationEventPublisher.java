package com.kfyty.boot.event;

import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.event.ApplicationEvent;
import com.kfyty.support.event.ApplicationEventPublisher;
import com.kfyty.support.event.ApplicationListener;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.ReflectUtil;

import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * 描述: 事件发布器默认实现
 *
 * @author kfyty725
 * @date 2021/6/21 16:56
 * @email kfyty725@hotmail.com
 */
@Component("applicationEventPublisher")
@SuppressWarnings({"unchecked", "rawtypes"})
public class DefaultApplicationEventPublisher implements ApplicationEventPublisher {
    /**
     * 注册的事件监听器
     */
    private final List<ApplicationListener> applicationListeners;

    public DefaultApplicationEventPublisher(@Autowired(required = false) List<ApplicationListener> applicationListeners) {
        this.applicationListeners = applicationListeners;
    }

    /**
     * 获取监听器的监听类型时，如果被 jdk 代理，则应使用原对象，否则无法获取泛型信息
     */
    @Override
    public void publishEvent(ApplicationEvent<?> event) {
        for (ApplicationListener applicationListener : this.applicationListeners) {
            Class<?> listenerClass = AopUtil.getSourceIfNecessary(applicationListener).getClass();
            Class<?> listenerType = ReflectUtil.getSuperGeneric(listenerClass, 0, -1, type -> type instanceof ParameterizedType && ((ParameterizedType) type).getRawType().equals(ApplicationListener.class));
            if(applicationListener instanceof EventListenerAnnotationListener) {
                listenerType = ((EventListenerAnnotationListener) applicationListener).getListenerType();
            }
            if(listenerType.equals(Object.class) || listenerType.equals(event.getClass())) {
                applicationListener.onApplicationEvent(event);
            }
        }
    }

    @Override
    public void registerEventListener(ApplicationListener<?> applicationListener) {
        this.applicationListeners.add(applicationListener);
    }
}
