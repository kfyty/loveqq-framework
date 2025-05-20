package com.kfyty.loveqq.framework.data.cache.core.proxy;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.OgnlUtil;
import com.kfyty.loveqq.framework.data.cache.core.Cache;
import com.kfyty.loveqq.framework.data.cache.core.CacheKeyFactory;
import com.kfyty.loveqq.framework.data.cache.core.NullValue;
import com.kfyty.loveqq.framework.data.cache.core.annotation.CacheClear;
import com.kfyty.loveqq.framework.data.cache.core.annotation.Cacheable;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 描述: 缓存代理
 *
 * @author kfyty725
 * @date 2024/7/4 10:51
 * @email kfyty725@hotmail.com
 */
@Order(Order.HIGHEST_PRECEDENCE)
public class CacheInterceptorProxy extends AbstractCacheInterceptorProxy {

    public CacheInterceptorProxy(Cache cache, CacheKeyFactory cacheKeyFactory, ScheduledExecutorService executorService) {
        super(cache, cacheKeyFactory, executorService);
    }

    @Override
    public Object around(String cacheableName,
                         String cacheClearName,
                         Cacheable cacheable,
                         CacheClear cacheClear,
                         Lazy<Map<String, Object>> context,
                         Method method,
                         ProceedingJoinPoint pjp) throws Throwable {
        // 先从缓存中获取
        if (cacheable != null) {
            Object cache = this.cache.get(cacheableName);
            if (cache != null) {
                return cache == NullValue.INSTANCE ? null : cache;                                                      // NullValue 返回 null
            }
        }

        // 前置清理
        this.preClear(cacheClearName, cacheClear);

        // 执行目标方法
        Object retValue = pjp.proceed();

        // 放入或删除缓存
        if (cacheable == null && cacheClear == null) {
            return retValue;
        }

        // 放入 context
        context.get().put(OGNL_RETURN_VALUE_KEY, retValue);

        // 缓存处理
        this.processCacheable(cacheableName, cacheable, retValue, method, pjp, context.get());
        this.processCacheClear(cacheClearName, cacheClear, method, pjp, context.get());

        return retValue;
    }

    protected void preClear(String cacheClearName, CacheClear cacheClear) {
        if (cacheClear != null && cacheClear.preClear()) {
            if (cacheClear.delay() <= 0) {
                this.cache.clear(cacheClearName);
            } else {
                this.executorService.schedule(() -> this.cache.clear(cacheClearName), cacheClear.delay(), TimeUnit.MILLISECONDS);
            }
        }
    }

    protected void processCacheable(String cacheName, Cacheable cacheable, Object retValue, Method method, ProceedingJoinPoint pjp, Map<String, Object> context) {
        if (cacheable == null || retValue == null && !cacheable.putIfNull()) {
            return;
        }
        if (CommonUtil.notEmpty(cacheable.condition()) && !OgnlUtil.getBoolean(cacheable.condition(), context)) {
            return;
        }
        if (retValue == null) {
            retValue = NullValue.INSTANCE;
        }
        Object prev = this.cache.putIfAbsent(cacheName, retValue, cacheable.ttl(), cacheable.unit());
        if (prev != null && !Objects.equals(prev, retValue)) {
            this.cache.clear(cacheName);
        }
    }

    protected void processCacheClear(String cacheName, CacheClear cacheClear, Method method, ProceedingJoinPoint pjp, Map<String, Object> context) {
        if (cacheClear == null) {
            return;
        }
        if (CommonUtil.notEmpty(cacheClear.condition()) && !OgnlUtil.getBoolean(cacheClear.condition(), context)) {
            return;
        }
        this.cache.clear(cacheName);
    }
}
