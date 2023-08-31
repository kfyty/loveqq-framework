package com.kfyty.boot.config.nacos.autoconfig;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.kfyty.core.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.autoconfig.env.PropertyContext;
import com.kfyty.core.exception.SupportException;
import com.kfyty.core.utils.PropertiesUtil;

import java.io.ByteArrayInputStream;

import static com.kfyty.core.utils.ClassLoaderUtil.classLoader;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 描述: nacos 属性初始化加载器
 *
 * @author kfyty725
 * @date 2022/6/1 10:58
 * @email kfyty725@hotmail.com
 */
public class NacosPropertyInitializeLoader implements BeanFactoryPostProcessor {
    @Autowired
    private NacosConfigProperties configProperties;

    @Autowired
    private ConfigService configService;

    @Autowired
    private Listener configListener;

    /**
     * 由于条件配置，导致 {@link PropertyContext} 加载过早，只能在此回调中处理
     *
     * @param beanFactory bean 工厂
     */
    @Override
    public void postProcessBeanFactory(BeanFactory beanFactory) {
        try {
            PropertyContext propertyContext = beanFactory.getBean(PropertyContext.class);
            for (NacosConfigProperties.Extension extensionConfig : configProperties.getExtensionConfigs()) {
                String config = this.configService.getConfig(extensionConfig.getDataId(), extensionConfig.getGroup(), 1000 * 10);

                this.loadConfig(config, propertyContext);

                if (extensionConfig.getRefresh() == null || extensionConfig.getRefresh()) {
                    this.configService.addListener(extensionConfig.getDataId(), extensionConfig.getGroup(), this.configListener);
                }
            }
        } catch (NacosException e) {
            throw new SupportException("load nacos config failed", e);
        }
    }

    /**
     * 加载配置中心的属性到本地
     *
     * @param config          配置
     * @param propertyContext 配置上下文
     */
    public void loadConfig(String config, PropertyContext propertyContext) {
        PropertiesUtil.load(
                new ByteArrayInputStream(config.getBytes(UTF_8)),
                classLoader(this.getClass()),
                null,
                (p, c) -> p.forEach((k, v) -> propertyContext.setProperty(k.toString(), v.toString()))
        );
    }
}
