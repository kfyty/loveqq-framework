package com.kfyty.loveqq.framework.boot.config.nacos.autoconfig;

import com.alibaba.nacos.api.config.ConfigService;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.boostrap.BootstrapConfiguration;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;
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
    /**
     * nacos config server address.
     */
    private String serverAddr;

    /**
     * nacos config cluster name.
     */
    private String clusterName;

    /**
     * the nacos authentication username.
     */
    private String username;

    /**
     * the nacos authentication password.
     */
    private String password;

    /**
     * namespace, separation configuration of different environments.
     */
    private String namespace;

    /**
     * access key for namespace.
     */
    private String accessKey;

    /**
     * secret key for namespace.
     */
    private String secretKey;

    /**
     * endpoint for Nacos, the domain name of a service, through which the server address
     * can be dynamically obtained.
     */
    private String endpoint;

    /**
     * the suffix of nacos config dataId, also the file extension of config content.
     */
    private String fileExtension;

    /**
     * encode for nacos config content.
     */
    private String encode;

    /**
     * nacos maximum number of tolerable server reconnection errors.
     */
    private String maxRetry;

    /**
     * nacos get config failure retry time.
     */
    private String configRetryTime;

    /**
     * nacos get config long poll timeout.
     */
    private String configLongPollTimeout;

    /**
     * If you want to pull it yourself when the program starts to get the configuration
     * for the first time, and the registered Listener is used for future configuration
     * updates, you can keep the original code unchanged, just add the system parameter:
     * enableRemoteSyncConfig = "true" ( But there is network overhead); therefore we
     * recommend that you use {@link ConfigService#getConfigAndSignListener} directly.
     */
    private boolean enableRemoteSyncConfig = false;

    /**
     * a set of extensional configurations
     */
    private List<Extension> extensionConfigs = new LinkedList<>();

    @Data
    public static class Extension {
        private String group;
        private String dataId;
        private Boolean refresh;
        private Long timeout = 10_000L;
    }
}
