package com.kfyty.loveqq.framework.data.cache.core.proxy.reactive;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import com.kfyty.loveqq.framework.core.utils.OgnlUtil;
import com.kfyty.loveqq.framework.data.cache.core.CacheKeyFactory;
import com.kfyty.loveqq.framework.data.cache.core.annotation.CacheClear;
import com.kfyty.loveqq.framework.data.cache.core.annotation.Cacheable;
import com.kfyty.loveqq.framework.data.cache.core.proxy.AbstractCacheInterceptorProxy;
import com.kfyty.loveqq.framework.data.cache.core.reactive.ReactiveCache;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 描述: 响应式缓存代理
 *
 * @author kfyty725
 * @date 2024/7/4 10:51
 * @email kfyty725@hotmail.com
 */
@Order(Order.HIGHEST_PRECEDENCE)
public class ReactiveCacheInterceptorProxy extends AbstractCacheInterceptorProxy {

    public ReactiveCacheInterceptorProxy(ReactiveCache cache, CacheKeyFactory cacheKeyFactory, ScheduledExecutorService executorService) {
        super(cache, cacheKeyFactory, executorService);
    }

    @Override
    @SuppressWarnings({"unchecked", "ReactiveStreamsUnusedPublisher"})
    public Object around(String cacheableName,
                         String cacheClearName,
                         Cacheable cacheable,
                         CacheClear cacheClear,
                         Lazy<Map<String, Object>> context,
                         Method method,
                         ProceedingJoinPoint pjp) throws Throwable {
        final boolean isMono = Mono.class.isAssignableFrom(method.getReturnType());
        if (cacheable == null) {
            return this.proceed(isMono, cacheableName, cacheClearName, cacheable, cacheClear, context, method, pjp);
        }
        if (isMono) {
            return this.getReactiveCache()
                    .getAsync(cacheableName)
                    .switchIfEmpty(Mono.defer(() -> (Mono<?>) this.proceed(true, cacheableName, cacheClearName, cacheable, cacheClear, context, method, pjp)));
        }
        return this.getReactiveCache()
                .getAsync(cacheableName)
                .flatMapMany(e -> Flux.fromIterable((Iterable<?>) e))
                .switchIfEmpty(Flux.defer(() -> this.proceed(false, cacheableName, cacheClearName, cacheable, cacheClear, context, method, pjp)));
    }

    /**
     * 继续调用目标方法，并放入缓存
     */
    @SuppressWarnings("rawtypes")
    protected Publisher proceed(final boolean isMono,
                                String cacheableName,
                                String cacheClearName,
                                Cacheable cacheable,
                                CacheClear cacheClear,
                                Lazy<Map<String, Object>> context,
                                Method method,
                                ProceedingJoinPoint pjp) {
        if (isMono) {
            return this.preClear(cacheClearName, cacheClear)
                    .then(invokeJoinPoint(pjp))
                    .flatMap(new CacheProcessor<>(cacheableName, cacheableName, cacheable, cacheClear, context.get(), method, pjp));
        }
        return this.preClear(cacheClearName, cacheClear)
                .thenMany(invokeJoinPointFlux(pjp))
                .collectList()
                .flatMap(new CacheProcessor<>(cacheableName, cacheableName, cacheable, cacheClear, context.get(), method, pjp))
                .flatMapIterable(e -> (Iterable<?>) e);
    }

    protected Mono<?> invokeJoinPoint(ProceedingJoinPoint pjp) {
        try {
            return (Mono<?>) pjp.proceed();
        } catch (Throwable e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    protected Flux<?> invokeJoinPointFlux(ProceedingJoinPoint pjp) {
        try {
            return (Flux<?>) pjp.proceed();
        } catch (Throwable e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    protected Mono<Void> preClear(String cacheClearName, CacheClear cacheClear) {
        if (cacheClear != null && cacheClear.preClear()) {
            if (cacheClear.delay() <= 0) {
                return this.getReactiveCache().clearAsync(cacheClearName);
            } else {
                this.executorService.schedule(() -> this.getReactiveCache().clearAsync(cacheClearName).subscribe(), cacheClear.delay(), TimeUnit.MILLISECONDS);
                return Mono.empty();
            }
        }
        return Mono.empty();
    }

    @RequiredArgsConstructor
    protected class CacheProcessor<T> implements Function<T, Mono<T>> {
        protected final String cacheableName;

        protected final String cacheClearName;

        protected final Cacheable cacheable;

        protected final CacheClear cacheClear;

        protected final Map<String, Object> context;

        protected final Method method;

        protected final ProceedingJoinPoint pjp;

        @Override
        public Mono<T> apply(T value) {
            if (cacheable == null && cacheClear == null) {
                return Mono.just(value);
            }
            context.put(OGNL_RETURN_VALUE_KEY, value);
            return this.processCacheable(cacheableName, cacheable, value, method, pjp, context)
                    .then(this.processCacheClear(cacheClearName, cacheClear, method, pjp, context))
                    .thenReturn(value);
        }

        protected Mono<Void> processCacheable(String cacheName, Cacheable cacheable, Object retValue, Method method, ProceedingJoinPoint pjp, Map<String, Object> context) {
            if (cacheable == null || retValue == null) {
                return Mono.empty();
            }
            if (CommonUtil.notEmpty(cacheable.condition()) && !OgnlUtil.getBoolean(cacheable.condition(), context)) {
                return Mono.empty();
            }
            final ReactiveCache reactiveCache = getReactiveCache();
            return reactiveCache.putIfAbsentAsync(cacheName, retValue, cacheable.ttl(), cacheable.unit())
                    .flatMap(prev -> prev == null || Objects.equals(prev, retValue) ? Mono.empty() : reactiveCache.clearAsync(cacheName));
        }

        protected Mono<Void> processCacheClear(String cacheName, CacheClear cacheClear, Method method, ProceedingJoinPoint pjp, Map<String, Object> context) {
            if (cacheClear == null) {
                return Mono.empty();
            }
            if (CommonUtil.notEmpty(cacheClear.condition()) && !OgnlUtil.getBoolean(cacheClear.condition(), context)) {
                return Mono.empty();
            }
            return getReactiveCache().clearAsync(cacheName);
        }
    }
}
