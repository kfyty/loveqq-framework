package com.kfyty.loveqq.framework.boot.mq.rocket.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.StaticSessionCredentialsProvider;
import org.apache.rocketmq.client.apis.producer.ProducerBuilder;
import org.apache.rocketmq.client.apis.producer.TransactionChecker;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/12/09 21:54
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnProperty(value = "rocketmq.endpoints", matchIfNonNull = true)
public class RocketMQAutoConfiguration {
    @Autowired
    private RocketMQProperties rocketMQProperties;

    @Autowired(required = false)
    private TransactionChecker transactionChecker;

    @Bean(resolveNested = false, independent = true)
    public ClientConfiguration clientConfiguration() {
        ClientConfigurationBuilder configurationBuilder = ClientConfiguration.newBuilder()
                .setEndpoints(this.rocketMQProperties.getEndpoints())
                .enableSsl(this.rocketMQProperties.isSslEnabled());
        Mapping.from(this.rocketMQProperties.getNamespace()).whenNotEmpty(configurationBuilder::setNamespace);
        Mapping.from(this.rocketMQProperties.getRequestTimeout()).whenNotEmpty(configurationBuilder::setRequestTimeout);
        if (CommonUtil.notEmpty(this.rocketMQProperties.getAccessKey()) && CommonUtil.notEmpty(this.rocketMQProperties.getAccessSecret())) {
            configurationBuilder.setCredentialProvider(
                    CommonUtil.empty(this.rocketMQProperties.getSecurityToken())
                            ? new StaticSessionCredentialsProvider(this.rocketMQProperties.getAccessKey(), this.rocketMQProperties.getAccessSecret())
                            : new StaticSessionCredentialsProvider(this.rocketMQProperties.getAccessKey(), this.rocketMQProperties.getAccessSecret(), this.rocketMQProperties.getSecurityToken())
            );
        }
        return configurationBuilder.build();
    }

    @ConditionalOnMissingBean
    @Bean(resolveNested = false, independent = true)
    public ClientServiceProvider clientServiceProvider() {
        return ClientServiceProvider.loadService();
    }

    @Bean(destroyMethod = "close", resolveNested = false, independent = true)
    public RocketMQProducer rocketMQProducer(ClientConfiguration clientConfiguration, ClientServiceProvider provider) throws Exception {
        ProducerBuilder producerBuilder = provider.newProducerBuilder();
        producerBuilder.setClientConfiguration(clientConfiguration);
        Mapping.from(this.transactionChecker).whenNotNull(producerBuilder::setTransactionChecker);
        Mapping.from(this.rocketMQProperties.getMaxAttempts()).whenNotNull(producerBuilder::setMaxAttempts);
        Mapping.from(this.rocketMQProperties.getTransactionTopics()).whenNotEmpty(producerBuilder::setTopics);
        return new RocketMQProducer(provider, producerBuilder.build());
    }
}
