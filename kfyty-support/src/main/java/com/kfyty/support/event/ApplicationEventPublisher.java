package com.kfyty.support.event;

/**
 * 描述: 事件发布器
 *
 * @author kfyty725
 * @date 2021/6/21 16:53
 * @email kfyty725@hotmail.com
 */
public interface ApplicationEventPublisher {

    void publishEvent(ApplicationEvent<?> event);

    void registerEventListener(ApplicationListener<?> applicationListener);
}
