package com.kfyty.loveqq.framework.boot.feign.autoconfig.factory;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.netflix.client.ClientFactory;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;
import feign.ribbon.LBClient;
import feign.ribbon.LBClientFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述: LoadBalancerClientFactory
 *
 * @author kfyty725
 * @date 2024/3/08 18:55
 * @email kfyty725@hotmail.com
 */
public class LoadBalancerClientFactory implements LBClientFactory {
    /**
     * 注册 {@link ZoneAwareLoadBalancer} 不要处理泛型
     */
    static {
        SimpleGeneric.registryIgnoredClass(ZoneAwareLoadBalancer.class);
    }

    private final Map<String, LBClient> clientCache = new ConcurrentHashMap<>();

    @Autowired
    private ZoneAwareLoadBalancer<Server> loadBalancer;

    @Override
    public LBClient create(String clientName) {
        LBClient client = this.clientCache.get(clientName);
        if (client != null) {
            return client;
        }
        IClientConfig config = ClientFactory.getNamedConfig(clientName, DisableAutoRetriesByDefaultClientConfig.class);
        LBClient newClient = LBClient.create(this.loadBalancer, config);
        clientCache.put(clientName, newClient);
        return newClient;
    }
}
