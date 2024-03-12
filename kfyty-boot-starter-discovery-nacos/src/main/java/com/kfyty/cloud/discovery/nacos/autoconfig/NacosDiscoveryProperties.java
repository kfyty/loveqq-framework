package com.kfyty.cloud.discovery.nacos.autoconfig;

import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnProperty;
import lombok.Data;

/**
 * 描述: nacos 属性
 *
 * @author kfyty725
 * @date 2024/03/10 10:58
 * @email kfyty725@hotmail.com
 */
@Data
@Component
@ConfigurationProperties("k.nacos.discovery")
@ConditionalOnProperty(value = "k.nacos.discovery.serverAddr", matchIfNonNull = true)
public class NacosDiscoveryProperties {
    private String serverAddr;
    private String namespace;
    private String ip;
    private String clusterName = "DEFAULT";
    private String groupName = "DEFAULT_GROUP";
}
