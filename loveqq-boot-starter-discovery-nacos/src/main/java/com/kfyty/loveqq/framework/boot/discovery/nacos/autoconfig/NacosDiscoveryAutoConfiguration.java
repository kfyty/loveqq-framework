package com.kfyty.loveqq.framework.boot.discovery.nacos.autoconfig;

import com.kfyty.loveqq.framework.boot.discovery.nacos.autoconfig.listener.NacosNamingEventListener;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnClass;

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
