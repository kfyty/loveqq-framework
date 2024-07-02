package com.kfyty.loveqq.framework.data.cache.redis;

import com.kfyty.loveqq.framework.data.cache.core.AbstractCache;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;

import java.util.concurrent.TimeUnit;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/7/4 14:28
 * @email kfyty725@hotmail.com
 */
public class RedisCache extends AbstractCache {
    private final RMapCache<String, Object> redisCache;

    public RedisCache(RedissonClient redissonClient) {
        this.redisCache = redissonClient.getMapCache("loveqq-redis-cache");
    }

    public RedisCache(RedissonClient redissonClient, Codec codec) {
        this.redisCache = redissonClient.getMapCache("loveqq-redis-cache", codec);
    }

    @Override
    public void put(String name, Object value, long ttl, TimeUnit timeUnit) {
        this.redisCache.put(name, value, ttl, timeUnit);
    }

    @Override
    public Object putIfAbsent(String name, Object value, long ttl, TimeUnit timeUnit) {
        return this.redisCache.putIfAbsent(name, value, ttl, timeUnit);
    }

    @Override
    public void clear(String name) {
        this.redisCache.fastRemove(name);
    }

    @Override
    public void clear() {
        this.redisCache.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T getInternal(String name) {
        return (T) this.redisCache.get(name);
    }
}
