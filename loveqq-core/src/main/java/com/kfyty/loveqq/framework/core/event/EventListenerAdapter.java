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
     * @param listener 事件监听器
     * @return 适配后的监听器
     */
    @SuppressWarnings("rawtypes")
    default ApplicationListener<?> adapt(ApplicationListener<ApplicationEvent<?>> source, ApplicationListener<ApplicationEvent<?>> listener) {
        if (EventListenerAnnotationListener.class.isInstance(source)) {
            return this.adapt((EventListenerAnnotationListener) (ApplicationListener) source, listener);
        }
        return listener;
    }

    /**
     * 事件监听器适配
     *
     * @param source   原始的监听器
     * @param listener 事件监听器
     * @return 适配后的监听器
     */
    default ApplicationListener<?> adapt(EventListenerAnnotationListener source, ApplicationListener<ApplicationEvent<?>> listener) {
        return listener;
    }
}
