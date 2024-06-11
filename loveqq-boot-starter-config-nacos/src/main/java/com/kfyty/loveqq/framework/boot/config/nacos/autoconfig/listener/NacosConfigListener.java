package com.kfyty.loveqq.framework.boot.config.nacos.autoconfig.listener;

import com.alibaba.nacos.api.config.listener.AbstractSharedListener;
import com.kfyty.loveqq.framework.boot.config.nacos.autoconfig.NacosPropertyLoaderBeanPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.loveqq.framework.core.event.PropertyConfigRefreshedEvent;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;

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
            this.nacosPropertyLoaderBeanPostProcessor.loadConfig(configInfo, group, true);
            this.applicationContext.publishEvent(new PropertyConfigRefreshedEvent(this.applicationContext));
        } catch (Exception e) {
            throw new ResolvableException("refresh config failed", e);
        }
    }
}
