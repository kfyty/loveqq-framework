package com.kfyty.loveqq.framework.data.cache.core;

import java.util.concurrent.TimeUnit;

/**
 * 描述: 缓存，实现应该是线程安全的
 *
 * @author kfyty725
 * @date 2024/7/4 20:41
 * @email kfyty725@hotmail.com
 */
public interface Cache extends AutoCloseable {
    /**
     * 获取缓存
     *
     * @param name 缓存名称
     * @return cache value
     */
    <T> T get(String name);

    /**
     * 获取缓存
     *
     * @param name  缓存名称
     * @param clazz 缓存类型
     * @return cache value
     */
    <T> T get(String name, Class<T> clazz);

    /**
     * 添加缓存，长期有效
     *
     * @param name  cache name
     * @param value cache value
     */
    default void put(String name, Object value) {
        this.put(name, value, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * 缓存不存在时添加缓存，长期有效
     *
     * @param name  cache name
     * @param value cache value
     * @return 是否放入缓存成功
     */
    default Object putIfAbsent(String name, Object value) {
        return this.putIfAbsent(name, value, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * 添加缓存并设置有效时间
     *
     * @param name  cache name
     * @param value cache value
     * @param ttl   cache time
     */
    default void put(String name, Object value, long ttl) {
        this.put(name, value, ttl, TimeUnit.SECONDS);
    }

    /**
     * 添加缓存并设置有效时间
     *
     * @param name     cache name
     * @param value    cache value
     * @param ttl      cache time
     * @param timeUnit time unit
     */
    void put(String name, Object value, long ttl, TimeUnit timeUnit);

    /**
     * 缓存不存在时添加缓存并设置有效时间
     *
     * @param name  cache name
     * @param value cache value
     * @param ttl   cache time
     * @return 是否放入缓存成功
     */
    default Object putIfAbsent(String name, Object value, long ttl) {
        return this.putIfAbsent(name, value, ttl, TimeUnit.SECONDS);
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
    Object putIfAbsent(String name, Object value, long ttl, TimeUnit timeUnit);

    /**
     * 移除指定缓存
     *
     * @param name cache name
     */
    void clear(String name);

    /**
     * 移除全部缓存
     */
    void clear();

    /**
     * 移除全部缓存
     */
    @Override
    default void close() throws Exception {
        this.clear();
    }
}
