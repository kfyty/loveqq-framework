package com.kfyty.loveqq.framework.data.cache.redis.reactive;

import com.kfyty.loveqq.framework.data.cache.core.reactive.AbstractReactiveCache;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.Codec;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/7/4 14:28
 * @email kfyty725@hotmail.com
 */
public class ReactiveRedisCache extends AbstractReactiveCache {
    private final RMapCacheReactive<String, Object> redisCache;

    public ReactiveRedisCache(RedissonReactiveClient redissonClient) {
        this.redisCache = redissonClient.getMapCache("loveqq-reactive-redis-cache");
    }

    public ReactiveRedisCache(RedissonReactiveClient redissonClient, Codec codec) {
        this.redisCache = redissonClient.getMapCache("loveqq-reactive-redis-cache", codec);
    }

    @Override
    public Mono<Void> putAsync(String name, Object value, long ttl, TimeUnit timeUnit) {
        return this.redisCache.put(name, value, ttl, timeUnit).then();
    }

    @Override
    public Mono<Object> putIfAbsentAsync(String name, Object value, long ttl, TimeUnit timeUnit) {
        return this.redisCache.putIfAbsent(name, value, ttl, timeUnit);
    }

    @Override
    public Mono<Void> clearAsync(String name) {
        return this.redisCache.remove(name).then();
    }

    @Override
    public Mono<Void> clearAsync() {
        return this.redisCache.delete().then();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> Mono<T> getInternalAsync(String name) {
        Mono<T> cache = (Mono<T>) this.redisCache.get(name);
        return cache == null ? Mono.empty() : cache;
    }
}
