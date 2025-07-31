package com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer;

/**
 * 描述: 负载均衡选择器
 *
 * @author kfyty725
 * @date 2023/9/10 22:00
 * @email kfyty725@hotmail.com
 */
public interface LoadBalanceChooser {
    /**
     * 选择一个可用的服务实例
     *
     * @param serviceId 服务 id
     * @return 服务实例
     */
    ServerInstance choose(String serviceId);
}
