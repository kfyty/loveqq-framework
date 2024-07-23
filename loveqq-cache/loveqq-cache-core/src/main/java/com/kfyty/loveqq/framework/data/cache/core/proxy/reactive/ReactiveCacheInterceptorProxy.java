package com.kfyty.loveqq.framework.data.cache.core.proxy.reactive;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.proxy.aop.MethodAroundAdvice;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import com.kfyty.loveqq.framework.core.utils.OgnlUtil;
import com.kfyty.loveqq.framework.data.cache.core.NullValue;
import com.kfyty.loveqq.framework.data.cache.core.annotation.CacheClear;
import com.kfyty.loveqq.framework.data.cache.core.annotation.Cacheable;
import com.kfyty.loveqq.framework.data.cache.core.proxy.AbstractCacheInterceptorProxy;
import com.kfyty.loveqq.framework.data.cache.core.reactive.ReactiveCache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 描述: 响应式缓存代理
 *
 * @author kfyty725
 * @date 2024/7/4 10:51
 * @email kfyty725@hotmail.com
 */
@Order(Order.HIGHEST_PRECEDENCE)
public class ReactiveCacheInterceptorProxy extends AbstractCacheInterceptorProxy implements MethodAroundAdvice {

    public ReactiveCacheInterceptorProxy(ReactiveCache cache, ScheduledExecutorService executorService) {
        super(cache, executorService);
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
        if (cacheable == null) {
            return this.proceed(cacheableName, cacheClearName, cacheable, cacheClear, context, method, pjp);
        }
        if (Mono.class.isAssignableFrom(method.getReturnType())) {
            return this.getReactiveCache().getAsync(cacheableName)
                    .switchIfEmpty(Mono.defer(() -> (Mono<?>) this.proceed(cacheableName, cacheClearName, cacheable, cacheClear, context, method, pjp)));
        }
        return this.getReactiveCache().getAsync(cacheableName)
                .flatMapMany(e -> Flux.fromIterable((Iterable<?>) e))
                .switchIfEmpty(Flux.defer(() -> this.proceed(cacheableName, cacheClearName, cacheable, cacheClear, context, method, pjp)));
    }

    /**
     * 继续调用目标方法，并放入缓存
     */
    @SuppressWarnings("rawtypes")
    protected Publisher proceed(String cacheableName,
                                String cacheClearName,
                                Cacheable cacheable,
                                CacheClear cacheClear,
                                Lazy<Map<String, Object>> context,
                                Method method,
                                ProceedingJoinPoint pjp) {
        Mono<?> publisher;
        boolean isMono = Mono.class.isAssignableFrom(method.getReturnType());
        if (isMono) {
            publisher = this.preClear(cacheClearName, cacheClear).then(invokeJoinPoint(pjp));
        } else {
            publisher = this.preClear(cacheClearName, cacheClear).thenMany(invokeJoinPointFlux(pjp)).collectList();
        }

        publisher = publisher
                .flatMap(value -> {
                    if (cacheable == null && cacheClear == null) {
                        return Mono.just(value);
                    }
                    context.get().put(OGNL_RETURN_VALUE_KEY, value);
                    return this.processCacheable(cacheableName, cacheable, value, method, pjp, context.get())
                            .then(this.processCacheClear(cacheClearName, cacheClear, method, pjp, context.get()))
                            .thenReturn(value);
                });

        return isMono ? publisher : publisher.flatMapIterable(e -> (Iterable<?>) e);
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
            if (cacheClear.preClearTimeout() <= 0) {
                return this.getReactiveCache().clearAsync(cacheClearName);
            } else {
                this.executorService.execute(() -> Mono.just(cacheClearName).delayElement(Duration.ofMillis(cacheClear.preClearTimeout())).flatMap(this.getReactiveCache()::clearAsync).subscribe());
                return Mono.empty();
            }
        }
        return Mono.empty();
    }

    protected Mono<Void> processCacheable(String cacheName, Cacheable cacheable, Object retValue, Method method, ProceedingJoinPoint pjp, Map<String, Object> context) {
        if (cacheable == null || retValue == null && !cacheable.putIfNull()) {
            return Mono.empty();
        }
        if (CommonUtil.notEmpty(cacheable.condition()) && !OgnlUtil.getBoolean(cacheable.condition(), context)) {
            return Mono.empty();
        }
        if (retValue == null) {
            retValue = NullValue.INSTANCE;
        }
        return Mono.just(retValue)
                .flatMap(value -> {
                    Mono<Object> prev = this.getReactiveCache().putIfAbsentAsync(cacheName, value, cacheable.ttl(), cacheable.unit());
                    return prev == null ? Mono.empty() : prev.doOnNext(prevValue -> {
                                if (!Objects.equals(prevValue, value)) {
                                    this.cache.clear(cacheName);
                                }
                            })
                            .then();
                });
    }

    protected Mono<Void> processCacheClear(String cacheName, CacheClear cacheClear, Method method, ProceedingJoinPoint pjp, Map<String, Object> context) {
        if (cacheClear == null) {
            return Mono.empty();
        }
        if (CommonUtil.notEmpty(cacheClear.condition()) && !OgnlUtil.getBoolean(cacheClear.condition(), context)) {
            return Mono.empty();
        }
        return this.getReactiveCache().clearAsync(cacheName);
    }
}
