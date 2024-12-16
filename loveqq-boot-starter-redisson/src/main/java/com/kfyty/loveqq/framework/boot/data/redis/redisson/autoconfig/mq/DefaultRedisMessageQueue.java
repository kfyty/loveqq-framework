package com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig.mq;

import com.kfyty.loveqq.framework.core.autoconfig.DestroyBean;
import com.kfyty.loveqq.framework.core.support.Triple;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 描述: redis 事件发布器
 *
 * @author kfyty725
 * @date 2022/5/31 20:49
 * @email kfyty725@hotmail.com
 */
public class DefaultRedisMessageQueue implements RedisMessageQueue, DestroyBean {
    /**
     * redis 客户端
     */
    private final RedissonClient redissonClient;

    /**
     * 监听器执行线程池
     */
    private final ExecutorService executorService;

    /**
     * 延迟队列
     */
    private final Map<String, Triple<RBlockingQueue<Object>, RDelayedQueue<Object>, Queue<MessageListener>>> queueMap;

    public DefaultRedisMessageQueue(RedissonClient redissonClient, ExecutorService executorService) {
        this.redissonClient = redissonClient;
        this.executorService = executorService;
        this.queueMap = new ConcurrentHashMap<>();
    }

    @Override
    public void send(String topic, Object message) {
        this.obtainQueue(topic).getValue().offer(message, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public void send(String topic, Object message, long delay, TimeUnit timeUnit) {
        this.obtainQueue(topic).getValue().offer(message, delay, timeUnit);
    }

    @Override
    public void registryMessageListener(String topic, MessageListener listener) {
        Triple<RBlockingQueue<Object>, RDelayedQueue<Object>, Queue<MessageListener>> triple = this.obtainQueue(topic);
        synchronized (triple) {
            Queue<MessageListener> listeners = triple.getTriple();
            listeners.offer(listener);
            if (triple.getTriple().size() == 1) {
                triple.getKey().subscribeOnElements(message -> {
                    CompletableFuture<?>[] futures = listeners.stream().map(e -> CompletableFuture.runAsync(() -> e.onMessage(message), this.executorService)).toArray(CompletableFuture[]::new);
                    return CompletableFuture.allOf(futures);
                });
            }
        }
    }

    @Override
    public void destroy() {
        for (Map.Entry<String, Triple<RBlockingQueue<Object>, RDelayedQueue<Object>, Queue<MessageListener>>> entry : this.queueMap.entrySet()) {
            entry.getValue().getValue().destroy();
        }
    }

    protected Triple<RBlockingQueue<Object>, RDelayedQueue<Object>, Queue<MessageListener>> obtainQueue(String topic) {
        return this.queueMap.computeIfAbsent(topic, k -> {
            RBlockingQueue<Object> queue = this.redissonClient.getBlockingQueue(k);
            RDelayedQueue<Object> delayedQueue = this.redissonClient.getDelayedQueue(queue);
            return new Triple<>(queue, delayedQueue, new LinkedBlockingQueue<>());
        });
    }
}
