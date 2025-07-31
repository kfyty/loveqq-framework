package com.kfyty.loveqq.framework.cloud.bootstrap.event;

import com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer.Server;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;

/**
 * 描述: 服务实例变更事件
 *
 * @author kfyty725
 * @date 2024/03/10 10:58
 * @email kfyty725@hotmail.com
 */
public class ServerEvent extends ApplicationEvent<Server> {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ServerEvent(Server source) {
        super(source);
    }
}
