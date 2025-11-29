package com.kfyty.loveqq.framework.data.cache.core.reactive;

import com.kfyty.loveqq.framework.data.cache.core.Cache;
import com.kfyty.loveqq.framework.data.cache.core.DefaultCache;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 描述: 响应式缓存默认实现，基于 {@link java.util.concurrent.ConcurrentHashMap}
 *
 * @author kfyty725
 * @date 2024/7/4 10:18
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class DefaultReactiveCache extends AbstractReactiveCache implements ReactiveCache {
    /**
     * 响应式缓存基于内存缓存实现
     */
    private final Cache cache;

    public DefaultReactiveCache(ScheduledExecutorService schedule) {
        this.cache = new DefaultCache(schedule);
    }

    @Override
    public Mono<Void> putAsync(String name, Object value, long ttl, TimeUnit timeUnit) {
        return Mono.fromRunnable(() -> this.cache.put(name, value, ttl, timeUnit));
    }

    @Override
    public Mono<Object> putIfAbsentAsync(String name, Object value, long ttl, TimeUnit timeUnit) {
        Object prev = this.cache.putIfAbsent(name, value, ttl, timeUnit);
        return prev == null ? Mono.empty() : Mono.just(prev);
    }

    @Override
    public Mono<Void> clearAsync(String name) {
        return Mono.fromRunnable(() -> this.cache.clear(name));
    }

    @Override
    public Mono<Void> clearAsync() {
        return Mono.fromRunnable(this.cache::clear);
    }

    @Override
    protected <T> Mono<T> getInternalAsync(String name) {
        T o = this.cache.get(name);
        return o == null ? Mono.empty() : Mono.just(o);
    }
}
