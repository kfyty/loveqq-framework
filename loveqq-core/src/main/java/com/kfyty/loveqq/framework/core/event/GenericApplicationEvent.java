package com.kfyty.loveqq.framework.core.event;

import lombok.Getter;

/**
 * 描述: 泛型事件支持，当发布为泛型事件时，将使用实际泛型(T)作为事件类型发布
 *
 * <p><b>
 * 子类扩展时，泛型的类型参数必须使用 T，否则将解析失败
 * </b></p>
 *
 * @author kfyty725
 * @date 2021/6/21 16:40
 * @email kfyty725@hotmail.com
 */
@Getter
public class GenericApplicationEvent<T, S> extends ApplicationEvent<S> {
    /**
     * 实际事件类型
     */
    private final T event;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public GenericApplicationEvent(T event, S source) {
        super(source);
        this.event = event;
    }

    /**
     * return event class type
     *
     * @return event type
     */
    public Class<?> getEventType() {
        return this.event.getClass();
    }
}
