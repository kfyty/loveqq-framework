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

    @Bean(destroyMethod = "shutDown", resolveNested = false, ignoredAutowired = true)
    public ConfigService nacosConfigService(NacosConfigProperties configProperties) throws NacosException {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, configProperties.getServerAddr());
        properties.put(PropertyKeyConst.NAMESPACE, configProperties.getNamespace());
        if (configProperties.getUsername() != null && configProperties.getPassword() != null) {
            properties.put(PropertyKeyConst.USERNAME, configProperties.getUsername());
            properties.put(PropertyKeyConst.PASSWORD, configProperties.getPassword());
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
