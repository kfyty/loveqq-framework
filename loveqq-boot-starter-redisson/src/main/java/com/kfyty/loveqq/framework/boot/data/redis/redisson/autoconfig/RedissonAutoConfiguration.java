package com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig.mq.DefaultRedisMessageQueue;
import com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig.mq.RedisMQMessageListenerRegistry;
import com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig.mq.RedisMessageQueue;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Import;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * 描述: redisson 自动配置类
 *
 * @author kfyty725
 * @date 2022/5/31 15:10
 * @email kfyty725@hotmail.com
 */
@Configuration
@Import(config = RedissonProperties.class)
@ConditionalOnBean(RedissonProperties.class)
public class RedissonAutoConfiguration {
    @Value("${k.server.virtualThread:true}")
    private boolean isVirtual;

    @Autowired("defaultThreadPoolExecutor")
    private ExecutorService defaultThreadPoolExecutor;

    @Autowired("redisListenerExecutor")
    private Optional<ExecutorService> redisListenerExecutor;

    @ConditionalOnMissingBean
    @Bean(resolveNested = false, independent = true)
    public Codec jsonJacksonCodec(@Autowired(required = false) ObjectMapper objectMapper) {
        if (objectMapper == null) {
            return new JsonJacksonCodec();
        }
        return new JsonJacksonCodec(objectMapper);
    }

    @ConditionalOnMissingBean
    @Bean(destroyMethod = "shutdown", resolveNested = false, independent = true)
    public RedissonClient redissonClient(RedissonProperties redissonProperties, @Autowired(required = false) Codec codec) {
        if (redissonProperties.getCodec() == null) {
            redissonProperties.setCodec(codec);
        }
        return Redisson.create(redissonProperties.buildConfig(this.isVirtual));
    }

    @ConditionalOnMissingBean
    @Bean(destroyMethod = "shutdown", resolveNested = false, independent = true)
    @ConditionalOnProperty(prefix = "k.redis.redisson", value = "reactive", havingValue = "true")
    public RedissonReactiveClient reactiveRedissonClient(RedissonClient redissonClient) {
        return redissonClient.reactive();
    }

    @Bean(resolveNested = false)
    public RedisMessageQueue redisMessageQueue(RedissonClient redissonClient) {
        return this.redisListenerExecutor
                .map(executorService -> new DefaultRedisMessageQueue(redissonClient, executorService))
                .orElseGet(() -> new DefaultRedisMessageQueue(redissonClient, this.defaultThreadPoolExecutor));
    }

    @Bean(resolveNested = false)
    public RedisMQMessageListenerRegistry redisMQMessageListenerRegistry(BeanFactory beanFactory, RedisMessageQueue redisMessageQueue) {
        return new RedisMQMessageListenerRegistry(beanFactory, redisMessageQueue);
    }
}
