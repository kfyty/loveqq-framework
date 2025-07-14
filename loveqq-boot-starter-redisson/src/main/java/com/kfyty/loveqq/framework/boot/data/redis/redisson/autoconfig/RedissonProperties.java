package com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.NestedConfigurationProperty;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;
import com.kfyty.loveqq.framework.core.lang.util.concurrent.VirtualThreadExecutorHolder;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.Setter;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.config.ReplicatedServersConfig;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.redisson.connection.balancer.LoadBalancer;

import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.load;
import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.newInstance;
import static java.util.Optional.ofNullable;

/**
 * 描述: redisson 配置属性
 *
 * @author kfyty725
 * @date 2022/5/31 14:49
 * @email kfyty725@hotmail.com
 */
@Setter
@Component
@ConfigurationProperties("k.redis.redisson")
@ConditionalOnProperty(prefix = "k.redis.redisson", value = "model", matchIfNonNull = true)
public class RedissonProperties extends Config {
    /**
     * 配置模式: 即下面的五种嵌套配置的属性名
     */
    private String model;

    /**
     * 负载均衡器实现的全限定名
     */
    private String loadBalancer;

    /**
     * 是否开启响应式客户端
     */
    private Boolean reactive;

    /**
     * 单机模式
     */
    @NestedConfigurationProperty
    private SingleServerConfig single;

    /**
     * 哨兵模式
     */
    @NestedConfigurationProperty
    private SentinelServersConfig sentinel;

    /**
     * 主从模式
     */
    @NestedConfigurationProperty
    private MasterSlaveServersConfig master;

    /**
     * 复制模式
     */
    @NestedConfigurationProperty
    private ReplicatedServersConfig replicated;

    /**
     * 集群模式
     */
    @NestedConfigurationProperty
    private ClusterServersConfig cluster;

    @Override
    protected SingleServerConfig getSingleServerConfig() {
        return this.single;
    }

    @Override
    protected SentinelServersConfig getSentinelServersConfig() {
        return this.sentinel;
    }

    @Override
    protected MasterSlaveServersConfig getMasterSlaveServersConfig() {
        return this.master;
    }

    @Override
    protected ReplicatedServersConfig getReplicatedServersConfig() {
        return this.replicated;
    }

    @Override
    protected ClusterServersConfig getClusterServersConfig() {
        return this.cluster;
    }

    public Config buildConfig(boolean isVirtual) {
        if (this.model.equals("single")) {
            this.setSingleServerConfig(this.single);
        }
        if (this.model.equals("sentinel")) {
            this.setSentinelServersConfig(this.sentinel);
        }
        if (this.model.equals("master")) {
            this.setMasterSlaveServersConfig(this.master);
        }
        if (this.model.equals("replicated")) {
            this.setReplicatedServersConfig(this.replicated);
        }
        if (this.model.equals("cluster")) {
            this.setClusterServersConfig(this.cluster);
        }
        if (CommonUtil.notEmpty(this.loadBalancer)) {
            LoadBalancer balancer = (LoadBalancer) newInstance(load(this.loadBalancer));
            ofNullable(this.sentinel).ifPresent(e -> e.setLoadBalancer(balancer));
            ofNullable(this.master).ifPresent(e -> e.setLoadBalancer(balancer));
            ofNullable(this.replicated).ifPresent(e -> e.setLoadBalancer(balancer));
            ofNullable(this.cluster).ifPresent(e -> e.setLoadBalancer(balancer));
        }
        if (isVirtual && CommonUtil.VIRTUAL_THREAD_SUPPORTED) {
            this.setExecutor(VirtualThreadExecutorHolder.getInstance());
            this.setNettyExecutor(VirtualThreadExecutorHolder.getInstance());
        }
        return this;
    }
}
