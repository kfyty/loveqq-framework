package com.kfyty.loveqq.framework.boot.discovery.nacos.autoconfig;

import com.alibaba.nacos.api.naming.PreservedMetadataKeys;
import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

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
@ConditionalOnProperty(value = "k.nacos.discovery", matchIfNotEmpty = true)
public class NacosDiscoveryProperties implements InitializingBean {
    /**
     * service name to registry.
     */
    private String service;

    /**
     * the nacos authentication username.
     */
    private String username;

    /**
     * the nacos authentication password.
     */
    private String password;

    /**
     * the domain name of a service, through which the server address can be dynamically obtained.
     */
    private String endpoint;

    /**
     * whether your service is a https service.
     */
    private boolean secure;

    /**
     * access key for namespace.
     */
    private String accessKey;

    /**
     * secret key for namespace.
     */
    private String secretKey;

    /**
     * nacos discovery server address.
     */
    private String serverAddr;

    /**
     * namespace, separation registry of different environments.
     */
    private String namespace;

    /**
     * The ip address your want to register for your service instance, needn't to set it if the auto detect ip works well.
     */
    private String ip;

    /**
     * The port your want to register for your service instance, needn't to set it if the auto detect port works well.
     */
    private int port = -1;

    /**
     * cluster name for nacos .
     */
    private String clusterName = "DEFAULT";

    /**
     * group name for nacos.
     */
    private String groupName = "DEFAULT_GROUP";

    /**
     * weight for service instance, the larger the value, the larger the weight.
     */
    private Double weight = 1.0D;

    /**
     * instance health status.
     */
    private Boolean healthy = true;

    /**
     * If instance is enabled to accept request.
     */
    private Boolean enabled = true;

    /**
     * If instance is ephemeral.
     */
    private Boolean ephemeral = true;

    /**
     * naming load from local cache at application start. true is load.
     */
    private String namingLoadCacheAtStart = "false";

    /**
     * nacos naming log file name.
     */
    private String logName;

    /**
     * nacos.common.processors config
     */
    private Integer commonProcessors = 2;

    /**
     * nacos-grpc-client-executor config
     */
    private GrpcClientPool grpcClientPool = new GrpcClientPool();

    /**
     * extra metadata to register.
     */
    private Map<String, String> metadata = new HashMap<>(4);

    /**
     * 多个服务发现注册
     */
    private Map<String, NacosDiscoveryProperties> discoveries;

    @Override
    public void afterPropertiesSet() {
        if (this.discoveries == null) {
            this.discoveries = new HashMap<>(4);
        }

        if (this.namespace != null && !this.namespace.isEmpty()) {
            this.discoveries.put("default", this);
        }

        for (Map.Entry<String, NacosDiscoveryProperties> entry : this.discoveries.entrySet()) {
            entry.getValue().getMetadata().put(PreservedMetadataKeys.REGISTER_SOURCE, "loveqq_cloud");
        }

        System.setProperty("nacos.common.processors", this.commonProcessors.toString());
        System.setProperty("nacos.remote.client.grpc.pool.core.size", this.grpcClientPool.getCore().toString());
        System.setProperty("nacos.remote.client.grpc.pool.max.size", this.grpcClientPool.getMaximum().toString());
    }

    @Data
    public static class GrpcClientPool {
        private Integer core = 2;
        private Integer maximum = 4;
    }
}
