package com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer;

import com.kfyty.loveqq.framework.cloud.bootstrap.event.ServerEvent;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述: 负载均衡服务默认实现
 *
 * @author kfyty725
 * @date 2023/9/10 22:00
 * @email kfyty725@hotmail.com
 */
@Component
@ConditionalOnMissingBean(LoadBalanceService.class)
public class DefaultLoadBalanceService implements LoadBalanceService, ApplicationListener<ServerEvent> {
    /**
     * 服务
     */
    private final Map<String, Server> servers;

    public DefaultLoadBalanceService() {
        this.servers = new ConcurrentHashMap<>();
    }

    @Override
    public void registry(Server server) {
        this.servers.put(server.getName(), server);
    }

    @Override
    public void registry(String serverId, ServerInstance instance) {
        Server server = this.servers.computeIfAbsent(serverId, key -> new Server(key, new LinkedList<>()));
        synchronized (server) {
            server.getInstances().removeIf(e -> e.getId().equals(instance.getId()));
            server.getInstances().add(instance);
        }
    }

    @Override
    public Server getServer(String serverId) {
        return this.servers.get(serverId);
    }

    @Override
    public Collection<Server> listServers() {
        return this.servers.values();
    }

    @Override
    public void onApplicationEvent(ServerEvent event) {
        this.registry(event.getSource());
    }
}
