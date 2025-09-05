package com.kfyty.loveqq.framework.core.event;

import com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext;

/**
 * 描述: 属性配置上下文刷新事件
 *
 * @author kfyty725
 * @date 2022/1/28 18:04
 * @email kfyty725@hotmail.com
 */
public class PropertyContextRefreshedEvent extends ApplicationEvent<PropertyContext> {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public PropertyContextRefreshedEvent(PropertyContext source) {
        super(source);
    }
}
