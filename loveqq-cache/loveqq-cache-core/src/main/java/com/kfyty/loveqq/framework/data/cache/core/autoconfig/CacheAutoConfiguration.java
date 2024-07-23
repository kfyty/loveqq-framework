package com.kfyty.loveqq.framework.data.cache.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Primary;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.thread.NamedThreadFactory;
import com.kfyty.loveqq.framework.data.cache.core.Cache;
import com.kfyty.loveqq.framework.data.cache.core.DefaultCache;
import com.kfyty.loveqq.framework.data.cache.core.proxy.CacheInterceptorProxy;
import com.kfyty.loveqq.framework.data.cache.core.proxy.reactive.ReactiveCacheInterceptorProxy;
import com.kfyty.loveqq.framework.data.cache.core.reactive.DefaultReactiveCache;
import com.kfyty.loveqq.framework.data.cache.core.reactive.ReactiveCache;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 描述: 缓存自动配置
 *
 * @author kfyty725
 * @date 2024/7/4 10:50
 * @email kfyty725@hotmail.com
 */
@Configuration
public class CacheAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public Cache defaultCache() {
        return new DefaultCache();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveCache defaultReactiveCache() {
        return new DefaultReactiveCache();
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService delayCacheClearScheduledService(@Value("${cache.scheduled.core:2}") int scheduledCore) {
        return new ScheduledThreadPoolExecutor(scheduledCore, new NamedThreadFactory("pre-cache-clear"));
    }

    @Bean
    public CacheInterceptorProxy cacheInterceptorProxy(Cache cache, @Autowired("delayCacheClearScheduledService") ScheduledExecutorService executorService) {
        return new CacheInterceptorProxy(cache, executorService);
    }

    @Bean
    public ReactiveCacheInterceptorProxy reactiveCacheInterceptorProxy(ReactiveCache cache, @Autowired("delayCacheClearScheduledService") ScheduledExecutorService executorService) {
        return new ReactiveCacheInterceptorProxy(cache, executorService);
    }
}
