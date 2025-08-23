package com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig.mq;

import com.kfyty.loveqq.framework.core.autoconfig.DestroyBean;
import com.kfyty.loveqq.framework.core.support.Triple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 描述: redis 事件发布器
 *
 * @author kfyty725
 * @date 2022/5/31 20:49
 * @email kfyty725@hotmail.com
 */
@Slf4j
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
                triple.getKey().subscribeOnElements(new ConsumerSubscribe(listeners, this.executorService));
            }
        }
    }

    @Override
    public void removeMessageListener(String topic, MessageListener listener) {
        this.obtainQueue(topic).getTriple().remove(listener);
    }

    @Override
    public void destroy() {
        for (Map.Entry<String, Triple<RBlockingQueue<Object>, RDelayedQueue<Object>, Queue<MessageListener>>> entry : this.queueMap.entrySet()) {
            log.info("Destroy RedisMQ Topic: {}", entry.getKey());
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

    @RequiredArgsConstructor
    private static class ConsumerSubscribe implements Function<Object, CompletionStage<Void>> {
        private int index = 0;
        private final Queue<MessageListener> listeners;
        private final ExecutorService executorService;

        @Override
        public CompletionStage<Void> apply(Object message) {
            if (this.index >= this.listeners.size()) {
                this.index = 0;
            }
            Map<MessageConsumeMode, List<MessageListener>> modeMap = this.listeners.stream().collect(Collectors.groupingBy(MessageListener::getMode));
            List<MessageListener> anyMode = modeMap.getOrDefault(MessageConsumeMode.ANY, Collections.emptyList());
            List<MessageListener> allMode = modeMap.getOrDefault(MessageConsumeMode.ALL, Collections.emptyList());
            if (allMode.isEmpty()) {
                MessageListener listener = anyMode.get(this.index++ % anyMode.size());
                return CompletableFuture.runAsync(() -> listener.onMessage(message), this.executorService);
            }
            if (!anyMode.isEmpty()) {
                allMode.add(anyMode.get(this.index++ % anyMode.size()));
            }
            CompletableFuture<?>[] futures = allMode.stream().map(e -> CompletableFuture.runAsync(() -> e.onMessage(message), this.executorService)).toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(futures);
        }
    }
}
