package com.kfyty.boot.config.nacos.autoconfig;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.kfyty.boot.config.nacos.autoconfig.listener.NacosConfigListener;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.boostrap.BootstrapConfiguration;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;

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

    @Bean(destroyMethod = "shutDown")
    public ConfigService nacosConfigService(NacosConfigProperties configProperties) throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", configProperties.getServerAddr());
        properties.put("namespace", configProperties.getNamespace());
        properties.put("fileExtension", configProperties.getFileExtension());
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
