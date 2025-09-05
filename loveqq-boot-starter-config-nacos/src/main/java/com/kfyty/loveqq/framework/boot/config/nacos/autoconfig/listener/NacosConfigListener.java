package com.kfyty.loveqq.framework.boot.config.nacos.autoconfig.listener;

import com.alibaba.nacos.api.config.listener.AbstractSharedListener;
import com.kfyty.loveqq.framework.boot.config.nacos.autoconfig.NacosPropertyLoaderBeanPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.aware.PropertyContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext;
import com.kfyty.loveqq.framework.core.event.PropertyContextRefreshedEvent;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;

/**
 * 描述: nacos 配置监听器
 *
 * @author kfyty725
 * @date 2022/5/30 14:55
 * @email kfyty725@hotmail.com
 */
public class NacosConfigListener extends AbstractSharedListener implements PropertyContextAware, ApplicationContextAware {
    /**
     * 配置上下文
     */
    private PropertyContext propertyContext;

    /**
     * 应用上下文
     */
    private ApplicationContext applicationContext;

    /**
     * nacos 属性后置处理器
     */
    @Autowired
    private NacosPropertyLoaderBeanPostProcessor nacosPropertyLoaderBeanPostProcessor;

    @Override
    public void setPropertyContext(GenericPropertiesContext propertiesContext) {
        this.propertyContext = propertiesContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void innerReceive(String dataId, String group, String configInfo) {
        try {
            this.nacosPropertyLoaderBeanPostProcessor.loadConfig(configInfo, group, true);
            this.applicationContext.publishEvent(new PropertyContextRefreshedEvent(this.propertyContext));
        } catch (Exception e) {
            throw new ResolvableException("refresh config failed", e);
        }
    }
}
