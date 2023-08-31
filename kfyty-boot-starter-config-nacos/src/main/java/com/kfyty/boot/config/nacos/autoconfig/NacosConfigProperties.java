package com.kfyty.boot.config.nacos.autoconfig;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 描述: nacos 属性
 *
 * @author kfyty725
 * @date 2022/6/1 10:58
 * @email kfyty725@hotmail.com
 */
@Data
public class NacosConfigProperties {
    private String serverAddr;

    private String namespace;

    private String fileExtension;

    private List<Extension> extensionConfigs = Collections.emptyList();

    @Data
    public static class Extension {
        private String group;
        private String dataId;
        private Boolean refresh;
    }
}
