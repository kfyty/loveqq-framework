package com.kfyty.core.event;

import com.kfyty.core.autoconfig.ApplicationContext;

/**
 * 描述: 属性配置刷新事件
 *
 * @author kfyty725
 * @date 2022/1/28 18:04
 * @email kfyty725@hotmail.com
 */
public class PropertyConfigRefreshedEvent extends ApplicationEvent<ApplicationContext> {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public PropertyConfigRefreshedEvent(ApplicationContext source) {
        super(source);
    }
}
