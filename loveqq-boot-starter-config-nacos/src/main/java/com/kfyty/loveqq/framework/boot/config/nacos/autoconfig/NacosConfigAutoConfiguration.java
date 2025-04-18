package com.kfyty.loveqq.framework.boot.config.nacos.autoconfig;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.kfyty.loveqq.framework.boot.config.nacos.autoconfig.listener.NacosConfigListener;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.boostrap.BootstrapConfiguration;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;

import java.util.Properties;

/**
 * 描述: nacos 配置服务自动配置
 *
 * @author kfyty725
 * @date 2022/5/30 14:55
 * @email kfyty725@hotmail.com
 */
@BootstrapConfiguration
@ConditionalOnBean(NacosConfigProperties.class)
public class NacosConfigAutoConfiguration {

    @Bean(destroyMethod = "shutDown", resolveNested = false, independent = true)
    public ConfigService nacosConfigService(NacosConfigProperties configProperties) throws NacosException {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, configProperties.getServerAddr());
        properties.put(PropertyKeyConst.NAMESPACE, configProperties.getNamespace());
        properties.put(PropertyKeyConst.ENABLE_REMOTE_SYNC_CONFIG, Boolean.toString(configProperties.isEnableRemoteSyncConfig()));
        if (configProperties.getClusterName() != null) {
            properties.put(PropertyKeyConst.CLUSTER_NAME, configProperties.getClusterName());
        }
        if (configProperties.getEncode() != null) {
            properties.put(PropertyKeyConst.ENCODE, configProperties.getEncode());
        }
        if (configProperties.getMaxRetry() != null) {
            properties.put(PropertyKeyConst.MAX_RETRY, configProperties.getMaxRetry());
        }
        if (configProperties.getConfigRetryTime() != null) {
            properties.put(PropertyKeyConst.CONFIG_RETRY_TIME, configProperties.getConfigRetryTime());
        }
        if (configProperties.getConfigLongPollTimeout() != null) {
            properties.put(PropertyKeyConst.CONFIG_LONG_POLL_TIMEOUT, configProperties.getConfigLongPollTimeout());
        }
        if (configProperties.getUsername() != null && configProperties.getPassword() != null) {
            properties.put(PropertyKeyConst.USERNAME, configProperties.getUsername());
            properties.put(PropertyKeyConst.PASSWORD, configProperties.getPassword());
        }
        if (configProperties.getAccessKey() != null && configProperties.getSecretKey() != null) {
            properties.put(PropertyKeyConst.ACCESS_KEY, configProperties.getAccessKey());
            properties.put(PropertyKeyConst.SECRET_KEY, configProperties.getSecretKey());
        }
        if (configProperties.getEndpoint() != null) {
            if (configProperties.getEndpoint().indexOf(':') < 0) {
                properties.put(PropertyKeyConst.ENDPOINT, configProperties.getEndpoint());
            } else {
                int index = configProperties.getEndpoint().indexOf(":");
                properties.put(PropertyKeyConst.ENDPOINT, configProperties.getEndpoint().substring(0, index));
                properties.put(PropertyKeyConst.ENDPOINT_PORT, configProperties.getEndpoint().substring(index + 1));
            }
        }
        return NacosFactory.createConfigService(properties);
    }

    @Bean
    public NacosPropertyLoaderBeanPostProcessor nacosPropertyLoaderBeanPostProcessor() {
        return new NacosPropertyLoaderBeanPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public Listener nacosConfigListener() {
        return new NacosConfigListener();
    }
}
