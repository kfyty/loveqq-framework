package com.kfyty.core.event;

import java.util.EventObject;

/**
 * 描述: 事件
 *
 * @author kfyty725
 * @date 2021/6/21 16:40
 * @email kfyty725@hotmail.com
 */
public abstract class ApplicationEvent<T> extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ApplicationEvent(T source) {
        super(source);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getSource() {
        return (T) super.getSource();
    }
}
