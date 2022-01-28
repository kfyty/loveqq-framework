package com.kfyty.support.event;

import com.kfyty.support.autoconfig.ApplicationContext;

/**
 * 描述: 容器刷新完成事件
 *
 * @author kfyty725
 * @date 2022/1/28 18:04
 * @email kfyty725@hotmail.com
 */
public class ContextRefreshedEvent extends ApplicationEvent<ApplicationContext> {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ContextRefreshedEvent(ApplicationContext source) {
        super(source);
    }
}
