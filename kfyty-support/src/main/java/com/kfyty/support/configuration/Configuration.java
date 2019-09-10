package com.kfyty.support.configuration;

/**
 * 功能描述: 自动配置接口
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/26 19:18
 * @since JDK 1.8
 */
public abstract class Configuration {

    private boolean autoConfiguration;

    public void autoConfigurationAfterCheck() {

    }

    public final boolean isAutoConfiguration() {
        return this.autoConfiguration;
    }

    public final Configuration enableAutoConfiguration() {
        this.autoConfiguration = true;
        return this;
    }
}
