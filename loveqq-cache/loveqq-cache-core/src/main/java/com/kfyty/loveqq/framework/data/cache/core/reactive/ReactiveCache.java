package com.kfyty.loveqq.framework.data.cache.core.reactive;

import com.kfyty.loveqq.framework.data.cache.core.Cache;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * 描述: 响应式缓存，实现应该是线程安全的
 *
 * @author kfyty725
 * @date 2024/7/4 20:41
 * @email kfyty725@hotmail.com
 */
public interface ReactiveCache extends Cache {
    /**
     * 获取缓存
     *
     * @param name 缓存名称
     * @return cache value
     */
    <T> Mono<T> getAsync(String name);

    /**
     * 获取缓存
     *
     * @param name  缓存名称
     * @param clazz 缓存类型
     * @return cache value
     */
    <T> Mono<T> getAsync(String name, Class<T> clazz);

    /**
     * 添加缓存，长期有效
     *
     * @param name  cache name
     * @param value cache value
     */
    default Mono<Void> putAsync(String name, Object value) {
        return this.putAsync(name, value, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * 缓存不存在时添加缓存，长期有效
     *
     * @param name  cache name
     * @param value cache value
     * @return 是否放入缓存成功
     */
    default Mono<Object> putIfAbsentAsync(String name, Object value) {
        return this.putIfAbsentAsync(name, value, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * 添加缓存并设置有效时间
     *
     * @param name  cache name
     * @param value cache value
     * @param ttl   cache time
     */
    default Mono<Void> putAsync(String name, Object value, long ttl) {
        return this.putAsync(name, value, ttl, TimeUnit.SECONDS);
    }

    /**
     * 添加缓存并设置有效时间
     *
     * @param name     cache name
     * @param value    cache value
     * @param ttl      cache time
     * @param timeUnit time unit
     */
    Mono<Void> putAsync(String name, Object value, long ttl, TimeUnit timeUnit);

    /**
     * 缓存不存在时添加缓存并设置有效时间
     *
     * @param name  cache name
     * @param value cache value
     * @param ttl   cache time
     * @return 是否放入缓存成功
     */
    default Mono<Object> putIfAbsentAsync(String name, Object value, long ttl) {
        return this.putIfAbsentAsync(name, value, ttl, TimeUnit.SECONDS);
    }

    /**
     * 缓存不存在时添加缓存并设置有效时间
     *
     * @param name     cache name
     * @param value    cache value
     * @param ttl      cache time
     * @param timeUnit time unit
     * @return 上一个值
     */
    Mono<Object> putIfAbsentAsync(String name, Object value, long ttl, TimeUnit timeUnit);

    /**
     * 移除指定缓存
     *
     * @param name cache name
     */
    Mono<Void> clearAsync(String name);

    /**
     * 移除全部缓存
     */
    Mono<Void> clearAsync();

    /*-------------------------------------------------- 同步操作适配 --------------------------------------------------*/

    @Override
    @SuppressWarnings("unchecked")
    default <T> T get(String name) {
        return (T) this.getAsync(name).block();
    }

    @Override
    default <T> T get(String name, Class<T> clazz) {
        return this.getAsync(name, clazz).block();
    }

    @Override
    default void put(String name, Object value, long ttl, TimeUnit timeUnit) {
        this.putAsync(name, value, ttl, timeUnit).block();
    }

    @Override
    default Object putIfAbsent(String name, Object value, long ttl, TimeUnit timeUnit) {
        return this.putIfAbsentAsync(name, value, ttl, timeUnit).block();
    }

    @Override
    default void clear(String name) {
        this.clearAsync(name).block();
    }

    @Override
    default void clear() {
        this.clearAsync().block();
    }
}
