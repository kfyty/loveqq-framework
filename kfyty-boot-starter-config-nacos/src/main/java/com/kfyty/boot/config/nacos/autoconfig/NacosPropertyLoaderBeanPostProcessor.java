package com.kfyty.boot.config.nacos.autoconfig;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.kfyty.core.autoconfig.BeanPostProcessor;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.env.PropertyContext;
import com.kfyty.core.exception.SupportException;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.PropertiesUtil;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static com.kfyty.core.utils.ClassLoaderUtil.classLoader;
import static com.kfyty.core.utils.PropertiesUtil.IMPORT_KEY;
import static com.kfyty.core.utils.PropertiesUtil.isYaml;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 描述: nacos 属性加载 bean 后置处理器
 *
 * @author kfyty725
 * @date 2022/6/1 10:58
 * @email kfyty725@hotmail.com
 */
public class NacosPropertyLoaderBeanPostProcessor implements BeanPostProcessor {
    /**
     * nacos 配置属性
     */
    @Autowired
    private NacosConfigProperties configProperties;

    /**
     * nacos 配置服务客户端
     */
    @Autowired
    private ConfigService configService;

    /**
     * 配置监听器
     */
    @Autowired
    private Listener configListener;

    /**
     * 属性配置上下文
     */
    private PropertyContext propertyContext;

    /**
     * 加载配置中心的属性到本地
     *
     * @param config 配置
     */
    public void loadConfig(String config, String group, boolean isRefresh) {
        PropertiesUtil.load(
                isYaml('.' + this.configProperties.getFileExtension()),
                new ByteArrayInputStream(config.getBytes(UTF_8)),
                classLoader(this.getClass()),
                p -> this.loadIncludePropertyConfig(p, group, isRefresh),
                (p, c) -> p.forEach((k, v) -> this.propertyContext.setProperty(k.toString(), v.toString()))
        );
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (this.propertyContext == null && bean instanceof PropertyContext) {
            this.propertyContext = (PropertyContext) bean;
            this.loadNacosPropertyConfig(this.propertyContext);
        }
        return null;
    }

    /**
     * 加载 nacos 配置
     *
     * @param propertyContext 属性配置上下文
     */
    protected void loadNacosPropertyConfig(PropertyContext propertyContext) {
        for (NacosConfigProperties.Extension extensionConfig : this.configProperties.getExtensionConfigs()) {
            boolean isRefresh = extensionConfig.getRefresh() == null || extensionConfig.getRefresh();
            this.loadNacosPropertyConfig(extensionConfig.getDataId(), extensionConfig.getGroup(), extensionConfig.getTimeout(), isRefresh);
        }
    }

    /**
     * 加载 nacos 配置
     */
    protected void loadNacosPropertyConfig(String dataId, String group, long timeout, boolean isRefresh) {
        try {
            String config = this.configService.getConfig(dataId, group, timeout);

            this.loadConfig(config, group, isRefresh);

            if (isRefresh) {
                this.configService.addListener(dataId, group, this.configListener);
            }
        } catch (NacosException e) {
            throw new SupportException("load nacos config failed", e);
        }
    }

    /**
     * 加载导入的嵌套的配置
     */
    protected void loadIncludePropertyConfig(Properties config, String group, boolean isRefresh) {
        Collection<String> imports = config.containsKey(IMPORT_KEY) ? CommonUtil.split(config.getProperty(IMPORT_KEY), ",", true) : Collections.emptyList();
        for (String importDataId : imports) {
            this.loadNacosPropertyConfig(importDataId, group, 10_000, isRefresh);
        }
    }
}
