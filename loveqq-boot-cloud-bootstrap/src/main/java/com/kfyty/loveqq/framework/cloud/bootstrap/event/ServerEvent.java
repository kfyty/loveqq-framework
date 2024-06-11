package com.kfyty.loveqq.framework.cloud.bootstrap.event;

import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 描述: 服务实例变更事件
 *
 * @author kfyty725
 * @date 2024/03/10 10:58
 * @email kfyty725@hotmail.com
 */
public class ServerEvent extends ApplicationEvent<ServerEvent.Server> {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ServerEvent(Server source) {
        super(source);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Server {
        private String name;
        private List<Instance> instances;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Instance {
        private String id;
        private String ip;
        private int port;
        private Map<String, String> metadata;
    }
}
