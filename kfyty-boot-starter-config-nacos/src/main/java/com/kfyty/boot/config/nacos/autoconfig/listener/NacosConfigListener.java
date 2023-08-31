package com.kfyty.boot.config.nacos.autoconfig.listener;

import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.kfyty.boot.config.nacos.autoconfig.NacosPropertyInitializeLoader;
import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.env.PropertyContext;
import com.kfyty.core.event.ContextRefreshedEvent;
import com.kfyty.core.exception.SupportException;

/**
 * 描述: nacos 配置监听器
 *
 * @author kfyty725
 * @date 2022/5/30 14:55
 * @email kfyty725@hotmail.com
 */
public class NacosConfigListener extends AbstractListener {
    @Autowired
    private PropertyContext propertyContext;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private NacosPropertyInitializeLoader propertyInitializeLoader;

    @Override
    public void receiveConfigInfo(String configInfo) {
        try {
            this.propertyInitializeLoader.loadConfig(configInfo, this.propertyContext);
            this.applicationContext.publishEvent(new ContextRefreshedEvent(this.applicationContext));
        } catch (Exception e) {
            throw new SupportException("refresh config failed", e);
        }
    }
}
