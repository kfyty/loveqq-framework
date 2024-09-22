package com.kfyty.loveqq.framework.boot.discovery.nacos.autoconfig;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.kfyty.loveqq.framework.boot.discovery.nacos.autoconfig.listener.NacosNamingEventListener;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnClass;

import java.util.Properties;

/**
 * 描述: nacos 服务发现自动配置
 *
 * @author kfyty725
 * @date 2024/03/10 10:58
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnBean(NacosDiscoveryProperties.class)
public class NacosDiscoveryAutoConfiguration {

    @Bean(destroyMethod = "shutDown", resolveNested = false, independent = true)
    public NamingService nacosDiscoveryService(NacosDiscoveryProperties discoveryProperties) throws NacosException {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, discoveryProperties.getServerAddr());
        properties.put(PropertyKeyConst.NAMESPACE, discoveryProperties.getNamespace());
        if (discoveryProperties.getUsername() != null && discoveryProperties.getPassword() != null) {
            properties.put(PropertyKeyConst.USERNAME, discoveryProperties.getUsername());
            properties.put(PropertyKeyConst.PASSWORD, discoveryProperties.getPassword());
        }
        return NacosFactory.createNamingService(properties);
    }

    @Bean
    public NacosDiscoveryRegisterService nacosDiscoveryRegisterService() {
        return new NacosDiscoveryRegisterService();
    }

    @Bean
    @ConditionalOnClass("com.kfyty.loveqq.framework.cloud.bootstrap.event.ServerEvent")
    public NacosNamingEventListener serviceChangedEventListener() {
        return new NacosNamingEventListener();
    }
}
