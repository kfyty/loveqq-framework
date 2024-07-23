package com.kfyty.loveqq.framework.data.cache.redis.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Primary;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.data.cache.core.Cache;
import com.kfyty.loveqq.framework.data.cache.core.reactive.ReactiveCache;
import com.kfyty.loveqq.framework.data.cache.redis.RedisCache;
import com.kfyty.loveqq.framework.data.cache.redis.reactive.ReactiveRedisCache;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.Codec;

/**
 * 描述: redis 缓存自动配置
 *
 * @author kfyty725
 * @date 2024/7/4 10:50
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnBean(RedissonClient.class)
public class RedisCacheAutoConfiguration {
    @Autowired(required = false)
    private Codec codec;

    @Bean
    @Primary
    public Cache redisCache(RedissonClient redissonClient) {
        if (this.codec == null) {
            return new RedisCache(redissonClient);
        }
        return new RedisCache(redissonClient, this.codec);
    }

    @Bean
    @ConditionalOnBean(RedissonReactiveClient.class)
    public ReactiveCache reactiveRedisCache(RedissonReactiveClient redissonReactiveClient) {
        if (this.codec == null) {
            return new ReactiveRedisCache(redissonReactiveClient);
        }
        return new ReactiveRedisCache(redissonReactiveClient, this.codec);
    }
}
