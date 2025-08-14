package com.kfyty.loveqq.framework.boot.feign.autoconfig.factory;

import com.kfyty.loveqq.framework.boot.feign.autoconfig.FeignProperties;
import com.kfyty.loveqq.framework.boot.feign.autoconfig.override.LoveqqLBClient;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.netflix.client.ClientFactory;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.config.AggregatedConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.util.ConfigurationUtils;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;
import org.apache.commons.configuration.AbstractConfiguration;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述: LoadBalancerClientFactory
 *
 * @author kfyty725
 * @date 2024/3/08 18:55
 * @email kfyty725@hotmail.com
 */
public class LoadBalancerClientFactory {
    /**
     * 客户端缓存
     */
    private final Map<String, LoveqqLBClient> clientCache = new ConcurrentHashMap<>();

    @Autowired
    private FeignProperties feignProperties;

    @Autowired
    private ZoneAwareLoadBalancer<Server> loadBalancer;

    public LoveqqLBClient create(String clientName) {
        LoveqqLBClient client = this.clientCache.get(clientName);
        if (client != null) {
            return client;
        }
        IClientConfig config = this.buildClientConfig(clientName);
        LoveqqLBClient newClient = LoveqqLBClient.create(this.loadBalancer, config);
        clientCache.put(clientName, newClient);
        return newClient;
    }

    protected IClientConfig buildClientConfig(String clientName) {
        return ClientFactory.getNamedConfig(clientName, () -> {
            Properties properties = this.buildRibbonProperties(clientName);
            DefaultClientConfigImpl clientConfig = DefaultClientConfigImpl.getClientConfigWithDefaultValues(clientName);
            if (properties != null) {
                AbstractConfiguration instance = ConfigurationManager.getConfigInstance();
                if (!(instance instanceof AggregatedConfiguration)) {
                    ConfigurationUtils.loadProperties(properties, instance);
                } else {
                    ConcurrentMapConfiguration configuration = new ConcurrentMapConfiguration();
                    configuration.loadProperties(properties);
                    ((AggregatedConfiguration) instance).addConfiguration(configuration, clientName);
                }
            }
            return clientConfig;
        });
    }

    protected Properties buildRibbonProperties(String clientName) {
        Map<String, String> ribbon = Mapping.from(this.feignProperties.getRibbon()).notNullMap(e -> e.get(clientName)).getOr(this.feignProperties.getRibbon(), e -> e.get("default"));
        if (ribbon == null || ribbon.isEmpty()) {
            return null;
        }
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : ribbon.entrySet()) {
            properties.setProperty(clientName + '.' + CommonClientConfigKey.DEFAULT_NAME_SPACE + '.' + entry.getKey(), entry.getValue());
        }
        return properties;
    }
}
