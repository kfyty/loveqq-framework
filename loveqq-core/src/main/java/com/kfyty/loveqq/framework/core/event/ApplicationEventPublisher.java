package com.kfyty.loveqq.framework.core.event;

/**
 * 描述: 事件发布器
 *
 * @author kfyty725
 * @date 2021/6/21 16:53
 * @email kfyty725@hotmail.com
 */
public interface ApplicationEventPublisher {
    /**
     * 发布事件
     *
     * @param event 事件
     */
    void publishEvent(ApplicationEvent<?> event);

    /**
     * 注册事件监听器
     * 监听的事件会自动解析
     *
     * @param applicationListener 事件监听器
     */
    void registerEventListener(ApplicationListener<?> applicationListener);
}
