package com.kfyty.loveqq.framework.data.cache.core;

import com.kfyty.loveqq.framework.core.thread.NamedThreadFactory;
import com.kfyty.loveqq.framework.core.utils.LogUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/7/4 10:18
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class DefaultCache extends AbstractCache {
    /**
     * 缓存容器
     */
    private final Map<String, Object> cache;

    /**
     * 缓存超时时间
     */
    private final ScheduledThreadPoolExecutor schedule;

    /**
     * 构造器
     */
    public DefaultCache() {
        this.cache = new ConcurrentHashMap<>();
        this.schedule = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("cache-clear"));
        Runtime.getRuntime().addShutdownHook(new Thread(this.schedule::shutdown));
    }

    @Override
    public void put(String name, Object value, long ttl, TimeUnit timeUnit) {
        this.cache.put(name, value);
        this.addCacheTtlTask(name, ttl, timeUnit);
    }

    @Override
    public Object putIfAbsent(String name, Object value, long ttl, TimeUnit timeUnit) {
        Object exists = this.cache.putIfAbsent(name, value);
        if (exists == null) {
            this.addCacheTtlTask(name, ttl, timeUnit);
        }
        return exists;
    }

    @Override
    public void clear(String name) {
        this.cache.remove(name);
    }

    @Override
    public void clear() {
        this.cache.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T getInternal(String name) {
        return (T) this.cache.get(name);
    }

    protected void addCacheTtlTask(String name, long ttl, TimeUnit timeUnit) {
        if (ttl > 0) {
            this.schedule.schedule(new ClearCacheTask(name), ttl, timeUnit);
            LogUtil.logIfDebugEnabled(log, log -> log.debug("add ttl cache named: {}", name));
        }
    }

    @RequiredArgsConstructor
    protected class ClearCacheTask implements Runnable {
        private final String name;

        @Override
        public void run() {
            DefaultCache.this.clear(name);
            LogUtil.logIfDebugEnabled(log, log -> log.debug("Clear cache named: {}", name));
        }
    }
}
