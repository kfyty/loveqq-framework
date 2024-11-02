package com.kfyty.loveqq.framework.boot.dubbo.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.NestedConfigurationProperty;
import lombok.Data;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ConfigCenterConfig;
import org.apache.dubbo.config.ConsumerConfig;
import org.apache.dubbo.config.MetadataReportConfig;
import org.apache.dubbo.config.MetricsConfig;
import org.apache.dubbo.config.ModuleConfig;
import org.apache.dubbo.config.MonitorConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.SslConfig;
import org.apache.dubbo.config.TracingConfig;

import java.util.List;
import java.util.Map;

/**
 * 描述: dubbo 配置属性
 *
 * @author kfyty725
 * @date 2024/10/29 20:31
 * @email kfyty725@hotmail.com
 */
@Data
@Component
@ConfigurationProperties("dubbo")
public class DubboProperties {
    /**
     * Configuration properties for the application.
     */
    @NestedConfigurationProperty
    private ApplicationConfig application = new ApplicationConfig();

    /**
     * Configuration properties for the module.
     */
    @NestedConfigurationProperty
    private ModuleConfig module = new ModuleConfig();

    /**
     * Configuration properties for the monitor.
     */
    @NestedConfigurationProperty
    private MonitorConfig monitor = new MonitorConfig();

    /**
     * Configuration properties for metrics.
     */
    @NestedConfigurationProperty
    private MetricsConfig metrics = new MetricsConfig();

    /**
     * Configuration properties for tracing.
     */
    @NestedConfigurationProperty
    private TracingConfig tracing = new TracingConfig();

    /**
     * Configuration properties for ssl.
     */
    @NestedConfigurationProperty
    private SslConfig ssl = new SslConfig();

    /**
     * Configuration properties for the provider.
     */
    private List<ProviderConfig> provider;

    /**
     * Configuration properties for the consumer.
     */
    private List<ConsumerConfig> consumer;

    /**
     * Configuration properties for the registry.
     */
    private Map<String, RegistryConfig> registry;

    /**
     * Configuration properties for the config center.
     */
    private Map<String, ConfigCenterConfig> configCenter;

    /**
     * Configuration properties for the protocol.
     */
    private Map<String, ProtocolConfig> protocol;

    /**
     * Configuration properties for the metadata report.
     */
    private Map<String, MetadataReportConfig> metadataReport;
}
