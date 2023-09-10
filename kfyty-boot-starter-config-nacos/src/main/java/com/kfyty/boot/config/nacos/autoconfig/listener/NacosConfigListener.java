package com.kfyty.boot.config.nacos.autoconfig.listener;

import com.alibaba.nacos.api.config.listener.AbstractSharedListener;
import com.kfyty.boot.config.nacos.autoconfig.NacosPropertyLoaderBeanPostProcessor;
import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.core.event.ContextRefreshedEvent;
import com.kfyty.core.exception.SupportException;

/**
 * 描述: nacos 配置监听器
 *
 * @author kfyty725
 * @date 2022/5/30 14:55
 * @email kfyty725@hotmail.com
 */
public class NacosConfigListener extends AbstractSharedListener implements ApplicationContextAware {
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
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void innerReceive(String dataId, String group, String configInfo) {
        try {
            this.nacosPropertyLoaderBeanPostProcessor.loadConfig(configInfo);
            this.applicationContext.publishEvent(new ContextRefreshedEvent(this.applicationContext));
        } catch (Exception e) {
            throw new SupportException("refresh config failed", e);
        }
    }
}
