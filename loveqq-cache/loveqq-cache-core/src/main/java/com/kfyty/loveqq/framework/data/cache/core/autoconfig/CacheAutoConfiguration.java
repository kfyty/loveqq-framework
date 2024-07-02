package com.kfyty.loveqq.framework.data.cache.core.autoconfig;

import com.kfyty.loveqq.framework.aop.Advisor;
import com.kfyty.loveqq.framework.aop.support.annotated.AnnotationPointcutAdvisor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.data.cache.core.Cache;
import com.kfyty.loveqq.framework.data.cache.core.DefaultCache;
import com.kfyty.loveqq.framework.data.cache.core.annotation.CacheClear;
import com.kfyty.loveqq.framework.data.cache.core.annotation.Cacheable;

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
    @ConditionalOnMissingBean
    public Cache defaultCache() {
        return new DefaultCache();
    }

    @Bean
    @ConditionalOnMissingBean(name = "cacheInterceptorAdvisor")
    public Advisor cacheInterceptorAdvisor(Cache cache) {
        return new AnnotationPointcutAdvisor(new CacheInterceptorProxy(cache), Cacheable.class, CacheClear.class);
    }
}
