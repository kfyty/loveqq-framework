package com.kfyty.loveqq.framework.core.event;

/**
 * 描述: 监听器适配器
 *
 * @author kfyty725
 * @date 2021/6/21 16:43
 * @email kfyty725@hotmail.com
 */
public interface EventListenerAdapter {
    /**
     * 事件监听器适配
     *
     * @param source   原始的监听器
     * @param listener 事件监听器，可能是适配后的
     * @return 适配后的监听器
     */
    default ApplicationListener<?> adapt(ApplicationListener<ApplicationEvent<?>> source, ApplicationListener<ApplicationEvent<?>> listener) {
        if (listener instanceof EventListenerAnnotationListener) {
            return this.adapt(source, (EventListenerAnnotationListener) listener);
        }
        return listener;
    }

    /**
     * 事件监听器适配
     *
     * @param source   原始的监听器
     * @param listener 事件监听器，可能是适配后的
     * @return 适配后的监听器
     */
    default ApplicationListener<?> adapt(ApplicationListener<ApplicationEvent<?>> source, EventListenerAnnotationListener listener) {
        return listener;
    }
}
