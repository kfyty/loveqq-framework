package com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer;

import com.kfyty.loveqq.framework.cloud.bootstrap.event.ServerEvent;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;
import com.kfyty.loveqq.framework.core.event.ApplicationEventPublisher;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
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
@Slf4j
@Component
@ConditionalOnMissingBean(LoadBalanceService.class)
public class DefaultLoadBalanceServiceRegistry implements LoadBalanceService, ApplicationListener<ServerEvent>, InternalPriority {
    /**
     * 是否自动注册
     */
    @Value("${k.cloud.discovery.register:true}")
    protected boolean autoRegister;

    /**
     * 事件订阅发布器
     */
    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;

    /**
     * 服务
     */
    protected final Map<String, Server> servers;

    public DefaultLoadBalanceServiceRegistry() {
        this.servers = new ConcurrentHashMap<>();
    }

    @Override
    public void registry(Server server) {
        Server prev = this.servers.put(server.getName(), server);
        if (prev != null && !prev.equals(server)) {
            log.warn("The registry server already exists and replaced: {}", prev);
        }
        this.applicationEventPublisher.publishEvent(new ServerEvent(LoadBalanceService.class, server));
    }

    @Override
    public void registry(String serverId, ServerInstance instance) {
        Server server = this.servers.computeIfAbsent(serverId, key -> new Server(key, new LinkedList<>()));
        synchronized (server) {
            server.getInstances().removeIf(e -> e.getId().equals(instance.getId()));
            server.getInstances().add(instance);
        }
        this.applicationEventPublisher.publishEvent(new ServerEvent(LoadBalanceService.class, server));
    }

    @Override
    public Server deregister(String serverId) {
        Server removed = this.servers.remove(serverId);
        this.applicationEventPublisher.publishEvent(new ServerEvent(LoadBalanceService.class, new Server(serverId, Collections.emptyList())));
        return removed;
    }

    @Override
    public ServerInstance deregister(String serverId, String instanceId) {
        Server server = this.servers.get(serverId);
        if (server != null) {
            ServerInstance removed = null;
            synchronized (server) {
                for (Iterator<ServerInstance> i = server.getInstances().iterator(); i.hasNext(); ) {
                    ServerInstance instance = i.next();
                    if (instance.getId().equals(instanceId)) {
                        i.remove();
                        removed = instance;
                        break;
                    }
                }
            }
            this.applicationEventPublisher.publishEvent(new ServerEvent(LoadBalanceService.class, server));
            return removed;
        }
        return null;
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
        if (event.getPublisher() == LoadBalanceService.class) {
            return;
        }
        if (this.autoRegister) {
            this.registry(event.getSource());
        }
    }
}
