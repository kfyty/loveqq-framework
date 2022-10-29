package com.kfyty.boot.data.redis.redisson.autoconfig;

import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.Import;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

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
}
