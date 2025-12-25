package com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer;

import com.kfyty.loveqq.framework.cloud.bootstrap.discovery.ServiceRegistry;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 描述: 负载均衡服务
 *
 * @author kfyty725
 * @date 2023/9/10 22:00
 * @email kfyty725@hotmail.com
 */
public interface LoadBalanceService extends ServiceRegistry {
    /**
     * 根据服务id获取服务
     *
     * @param serverId 服务id
     * @return 服务
     */
    Server getServer(String serverId);

    /**
     * 获取全部的服务
     *
     * @return {@link Server}
     */
    Collection<Server> listServers();

    /**
     * 获取全部的服务实例
     *
     * @return {@link ServerInstance}
     */
    default Collection<ServerInstance> listServerInstances() {
        return listServers().stream().flatMap(e -> e.getInstances().stream()).collect(Collectors.toList());
    }
}
