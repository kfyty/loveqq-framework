package com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Import;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;

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

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public RedissonClient redissonClient(RedissonProperties redissonProperties) {
        return Redisson.create(redissonProperties.buildConfig());
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "k.redis.redisson", value = "reactive", havingValue = "true")
    public RedissonReactiveClient reactiveRedissonClient(RedissonClient redissonClient) {
        return redissonClient.reactive();
    }
}
