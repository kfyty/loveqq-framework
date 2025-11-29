package com.kfyty.loveqq.framework.data.cache.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext;
import com.kfyty.loveqq.framework.data.cache.core.Cache;
import com.kfyty.loveqq.framework.data.cache.core.CacheKeyFactory;
import com.kfyty.loveqq.framework.data.cache.core.DefaultCacheKeyFactory;
import com.kfyty.loveqq.framework.data.cache.core.proxy.CacheInterceptorProxy;
import com.kfyty.loveqq.framework.data.cache.core.proxy.reactive.ReactiveCacheInterceptorProxy;
import com.kfyty.loveqq.framework.data.cache.core.reactive.DefaultReactiveCache;
import com.kfyty.loveqq.framework.data.cache.core.reactive.ReactiveCache;

import java.util.concurrent.ScheduledExecutorService;

import static com.kfyty.loveqq.framework.core.lang.ConstantConfig.DEFAULT_SCHEDULED_THREAD_POOL_EXECUTOR;

/**
 * 描述: 缓存自动配置
 * 这里仅配置 {@link ReactiveCache} 即可，因为默认基于内存访问，适配的堵塞操作也不会出现线程异常。
 * 若是其他实现，需同时配置 {@link Cache} 以及 {@link ReactiveCache}，否则适配的堵塞操作可能不被线程允许
 *
 * @author kfyty725
 * @date 2024/7/4 10:50
 * @email kfyty725@hotmail.com
 */
@Configuration
public class CacheAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean(resolveNested = false, independent = true)
    public ReactiveCache defaultReactiveCache(@Autowired(DEFAULT_SCHEDULED_THREAD_POOL_EXECUTOR) ScheduledExecutorService executorService) {
        return new DefaultReactiveCache(executorService);
    }

    @ConditionalOnMissingBean
    @Bean(resolveNested = false, independent = true)
    public CacheKeyFactory cacheKeyFactory() {
        return new DefaultCacheKeyFactory();
    }

    @Bean(resolveNested = false, independent = true)
    public CacheInterceptorProxy cacheInterceptorProxy(PropertyContext propertyContext,
                                                       Cache cache,
                                                       CacheKeyFactory cacheKeyFactory,
                                                       @Autowired(DEFAULT_SCHEDULED_THREAD_POOL_EXECUTOR) ScheduledExecutorService executorService) {
        return new CacheInterceptorProxy(propertyContext, cache, cacheKeyFactory, executorService);
    }

    @Bean(resolveNested = false, independent = true)
    public ReactiveCacheInterceptorProxy reactiveCacheInterceptorProxy(PropertyContext propertyContext,
                                                                       ReactiveCache cache,
                                                                       CacheKeyFactory cacheKeyFactory,
                                                                       @Autowired(DEFAULT_SCHEDULED_THREAD_POOL_EXECUTOR) ScheduledExecutorService executorService) {
        return new ReactiveCacheInterceptorProxy(propertyContext, cache, cacheKeyFactory, executorService);
    }
}
