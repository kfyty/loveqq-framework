package com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述: 负载均衡选择器默认实现，基于轮训策略
 *
 * @author kfyty725
 * @date 2023/9/10 22:00
 * @email kfyty725@hotmail.com
 */
@Component
@RequiredArgsConstructor
@ConditionalOnMissingBean(LoadBalanceChooser.class)
public class DefaultLoadBalanceChooser implements LoadBalanceChooser {
    /**
     * 索引
     */
    private final AtomicInteger index;

    /**
     * 负载均衡服务
     */
    private final LoadBalanceService loadBalanceService;

    @Autowired
    public DefaultLoadBalanceChooser(LoadBalanceService loadBalanceService) {
        this(new AtomicInteger(0), loadBalanceService);
    }

    @Override
    public ServerInstance choose(String serviceId) {
        Server server = this.loadBalanceService.getServer(serviceId);
        if (server != null && CommonUtil.notEmpty(server.getInstances())) {
            int index = this.index.getAndIncrement();
            List<ServerInstance> instances = server.getInstances();
            return instances.get(index % instances.size());
        }
        return null;
    }
}
