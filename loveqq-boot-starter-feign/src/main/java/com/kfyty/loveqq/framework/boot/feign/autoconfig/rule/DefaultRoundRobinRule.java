package com.kfyty.loveqq.framework.boot.feign.autoconfig.rule;

import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.Server;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 描述: 默认的轮训规则
 *
 * @author kfyty725
 * @date 2024/3/08 18:55
 * @email kfyty725@hotmail.com
 */
public class DefaultRoundRobinRule extends AbstractLoadBalancerRule {
    /**
     * 索引
     */
    private final AtomicInteger index;

    public DefaultRoundRobinRule() {
        this.index = new AtomicInteger(0);
    }

    @Override
    public Server choose(Object key) {
        List<Server> servers = this.getLoadBalancer().getAllServers();
        List<Server> filtered = servers.stream()
                .filter(e -> e.getMetaInfo() != null && Objects.equals(e.getMetaInfo().getAppName(), key))
                .filter(e -> e.isAlive() && e.isReadyToServe())
                .collect(Collectors.toList());
        return filtered.isEmpty() ? null : filtered.get(this.index.getAndIncrement() % filtered.size());
    }
}
