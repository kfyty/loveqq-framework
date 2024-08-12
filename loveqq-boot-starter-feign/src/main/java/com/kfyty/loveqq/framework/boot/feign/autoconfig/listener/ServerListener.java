package com.kfyty.loveqq.framework.boot.feign.autoconfig.listener;

import com.kfyty.loveqq.framework.cloud.bootstrap.event.ServerEvent;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.EventListener;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 描述: 服务变更事件监听器
 *
 * @author kfyty725
 * @date 2024/3/08 18:55
 * @email kfyty725@hotmail.com
 */
@EventListener
public class ServerListener {
    @Autowired
    private ZoneAwareLoadBalancer<Server> loadBalancer;

    @EventListener
    public void onServerEvent(ServerEvent event) {
        synchronized (this) {
            Map<String, ServerEvent.Instance> instanceMap = event.getSource().getInstances().stream().collect(Collectors.toMap(e -> e.getIp() + ":" + e.getPort(), v -> v));
            List<Server> servers = this.loadBalancer.getAllServers();
            for (Server server : servers) {
                server.setAlive(instanceMap.containsKey(server.getId()));
                instanceMap.remove(server.getId());
            }

            for (ServerEvent.Instance instance : instanceMap.values()) {
                Server server = new Server(instance.getId());
                server.setHost(instance.getIp());
                server.setPort(instance.getPort());
                server.setAlive(true);
                this.loadBalancer.addServer(server);
            }
        }
    }
}
