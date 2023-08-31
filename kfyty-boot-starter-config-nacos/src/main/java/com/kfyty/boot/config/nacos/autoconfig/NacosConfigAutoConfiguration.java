package com.kfyty.boot.config.nacos.autoconfig;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.kfyty.boot.config.nacos.autoconfig.listener.NacosConfigListener;
import com.kfyty.boot.context.env.DefaultDataBinder;
import com.kfyty.boot.context.env.DefaultGenericPropertiesContext;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnProperty;
import com.kfyty.core.autoconfig.env.PropertyContext;
import com.kfyty.core.support.Instance;

import java.util.Properties;

/**
 * 描述: nacos 配置服务自动配置
 *
 * @author kfyty725
 * @date 2022/5/30 14:55
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnProperty(value = "k.nacos.config.serverAddr", matchIfNonNull = true)
public class NacosConfigAutoConfiguration {
    private static final String NACOS_CONFIG_PREFIX = "k.nacos.config";

    @Bean
    public NacosConfigProperties nacosConfigProperties(PropertyContext propertyContext) {
        EarlyDataBinder dataBinder = new EarlyDataBinder();
        NacosConfigProperties nacosConfigProperties = new NacosConfigProperties();
        try (DefaultGenericPropertiesContext genericPropertiesContext = new DefaultGenericPropertiesContext()) {
            genericPropertiesContext.setDataBinder(dataBinder);
            propertyContext.getProperties().forEach(genericPropertiesContext::setProperty);

            dataBinder.setPropertyContext(genericPropertiesContext);
            dataBinder.bind(new Instance(nacosConfigProperties), NACOS_CONFIG_PREFIX);
            return nacosConfigProperties;
        }
    }

    @Bean(destroyMethod = "shutDown")
    public ConfigService nacosConfigService(NacosConfigProperties configProperties) throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", configProperties.getServerAddr());
        properties.put("namespace", configProperties.getNamespace());
        properties.put("fileExtension", configProperties.getFileExtension());
        return NacosFactory.createConfigService(properties);
    }

    @Bean
    public NacosPropertyInitializeLoader nacosPropertyInitializeLoader() {
        return new NacosPropertyInitializeLoader();
    }

    @Bean
    @ConditionalOnMissingBean
    public Listener nacosConfigListener() {
        return new NacosConfigListener();
    }

    private static class EarlyDataBinder extends DefaultDataBinder {
        public EarlyDataBinder() {
            this.ignoreInvalidFields = false;
            this.ignoreUnknownFields = true;
        }
    }
}
