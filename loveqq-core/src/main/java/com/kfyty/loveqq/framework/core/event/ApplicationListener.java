package com.kfyty.loveqq.framework.core.event;

import java.util.EventListener;

/**
 * 描述: 事件监听器
 *
 * @author kfyty725
 * @date 2021/6/21 16:43
 * @email kfyty725@hotmail.com
 */
public interface ApplicationListener<E extends ApplicationEvent<?>> extends EventListener {
    /**
     * 事件监听器
     *
     * @param event 事件
     */
    void onApplicationEvent(E event);
}
