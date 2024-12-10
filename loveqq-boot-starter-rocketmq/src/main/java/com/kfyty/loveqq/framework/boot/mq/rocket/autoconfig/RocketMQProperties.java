package com.kfyty.loveqq.framework.boot.mq.rocket.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;
import lombok.Data;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.producer.ProducerBuilder;

import java.time.Duration;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/12/09 21:54
 * @email kfyty725@hotmail.com
 */
@Data
@Component
@ConfigurationProperties("rocketmq")
@ConditionalOnProperty(value = "rocketmq.endpoints", matchIfNonNull = true)
public class RocketMQProperties {
    /**
     * {@link ClientConfigurationBuilder#setEndpoints(String)}
     */
    private String endpoints;

    /**
     * {@link ClientConfigurationBuilder#setNamespace(String)}
     */
    private String namespace;

    /**
     * {@link ClientConfigurationBuilder#setRequestTimeout(Duration)}
     */
    private Duration requestTimeout;

    /**
     * {@link ProducerBuilder#setMaxAttempts(int)}
     */
    private Integer maxAttempts;

    /**
     * {@link ClientConfigurationBuilder#enableSsl(boolean)}
     */
    private boolean sslEnabled;

    /**
     * {@link org.apache.rocketmq.client.apis.SessionCredentials#accessKey}
     */
    private String accessKey;

    /**
     * {@link org.apache.rocketmq.client.apis.SessionCredentials#accessSecret}
     */
    private String accessSecret;

    /**
     * {@link org.apache.rocketmq.client.apis.SessionCredentials#securityToken}
     */
    private String securityToken;

    /**
     * 事务消息话题，必须预绑定生产者
     */
    private String[] transactionTopics;
}
