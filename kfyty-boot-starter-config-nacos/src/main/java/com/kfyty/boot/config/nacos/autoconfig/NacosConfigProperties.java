package com.kfyty.boot.config.nacos.autoconfig;

import com.kfyty.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.core.autoconfig.boostrap.BootstrapConfiguration;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnProperty;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * 描述: nacos 属性
 *
 * @author kfyty725
 * @date 2022/6/1 10:58
 * @email kfyty725@hotmail.com
 */
@Data
@BootstrapConfiguration
@ConfigurationProperties("k.nacos.config")
@ConditionalOnProperty(value = "k.nacos.config.serverAddr", matchIfNonNull = true)
public class NacosConfigProperties {
    private String serverAddr;

    private String namespace;

    private String fileExtension;

    private List<Extension> extensionConfigs = new LinkedList<>();

    @Data
    public static class Extension {
        private String group;
        private String dataId;
        private Boolean refresh;
        private Long timeout = 10_000L;
    }
}
