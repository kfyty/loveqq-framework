package com.kfyty.loveqq.framework.cloud.bootstrap.discovery;

import com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer.Server;
import com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer.ServerInstance;

/**
 * 描述: 服务注册
 *
 * @author kfyty725
 * @date 2023/9/10 22:00
 * @email kfyty725@hotmail.com
 */
public interface ServiceRegistry {
    /**
     * 注册服务
     *
     * @param server 服务
     */
    void registry(Server server);

    /**
     * 注册服务实例
     *
     * @param serverId 服务id
     * @param instance 服务实例
     */
    void registry(String serverId, ServerInstance instance);

    /**
     * 取消注册服务
     *
     * @param serverId 服务id
     * @return 取消注册的服务
     */
    Server deregister(String serverId);

    /**
     * 取消注册服务实例
     *
     * @param serverId   服务id
     * @param instanceId 服务实例id
     * @return 取消注册的服务实例
     */
    ServerInstance deregister(String serverId, String instanceId);
}
