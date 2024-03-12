package com.kfyty.cloud.discovery.nacos.autoconfig;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.kfyty.cloud.discovery.nacos.autoconfig.listener.NacosNamingEventListener;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;

import java.util.Properties;

/**
 * 描述: nacos 服务发现自动配置
 *
 * @author kfyty725
 * @date 2024/03/10 10:58
 * @email kfyty725@hotmail.com
 */
@Configuration
public class NacosDiscoveryAutoConfiguration {

    @Bean(destroyMethod = "shutDown")
    public NamingService nacosDiscoveryService(NacosDiscoveryProperties discoveryProperties) throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", discoveryProperties.getServerAddr());
        properties.put("namespace", discoveryProperties.getNamespace());
        return NacosFactory.createNamingService(properties);
    }

    @Bean
    public NacosDiscoveryRegisterService nacosDiscoveryRegisterService() {
        return new NacosDiscoveryRegisterService();
    }

    @Bean
    public NacosNamingEventListener serviceChangedEventListener() {
        return new NacosNamingEventListener();
    }
}
