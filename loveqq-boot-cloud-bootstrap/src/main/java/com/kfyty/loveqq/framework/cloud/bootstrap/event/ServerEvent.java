package com.kfyty.loveqq.framework.cloud.bootstrap.event;

import com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer.Server;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import lombok.Getter;

/**
 * 描述: 服务实例变更事件
 *
 * @author kfyty725
 * @date 2024/03/10 10:58
 * @email kfyty725@hotmail.com
 */
@Getter
public class ServerEvent extends ApplicationEvent<Server> {
    /**
     * 事件发布者，可能为空
     */
    private final Class<?> publisher;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ServerEvent(Server source) {
        this(null, source);
    }

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ServerEvent(Class<?> publisher, Server source) {
        super(source);
        this.publisher = publisher;
    }
}
