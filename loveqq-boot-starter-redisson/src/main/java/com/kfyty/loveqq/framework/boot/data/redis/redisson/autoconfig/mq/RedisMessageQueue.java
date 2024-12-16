package com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig.mq;

import java.util.concurrent.TimeUnit;

/**
 * 描述: redis 事件发布器
 *
 * @author kfyty725
 * @date 2022/5/31 20:49
 * @email kfyty725@hotmail.com
 */
public interface RedisMessageQueue {
    /**
     * 默认队列
     */
    String DEFAULT_QUEUE = "loveqq-framework-redis-message-queue";

    /**
     * 发送消息到队列
     *
     * @param message 消息
     */
    default void send(Object message) {
        this.send(DEFAULT_QUEUE, message);
    }

    /**
     * 发送消息到队列
     *
     * @param message 消息
     */
    default void send(Object message, long delay) {
        this.send(DEFAULT_QUEUE, message, delay);
    }

    /**
     * 发送消息到队列
     *
     * @param message 消息
     */
    default void send(Object message, long delay, TimeUnit timeUnit) {
        this.send(DEFAULT_QUEUE, message, delay, timeUnit);
    }

    /**
     * 发送消息到队列
     *
     * @param message 消息
     */
    void send(String topic, Object message);

    /**
     * 发送消息到队列
     *
     * @param message 消息
     */
    default void send(String topic, Object message, long delay) {
        this.send(topic, message, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 发送消息到队列
     *
     * @param message 消息
     */
    void send(String topic, Object message, long delay, TimeUnit timeUnit);

    /**
     * 注册消息监听器
     *
     * @param listener 监听器
     */
    default void registryMessageListener(MessageListener listener) {
        this.registryMessageListener(DEFAULT_QUEUE, listener);
    }

    /**
     * 注册消息监听器
     *
     * @param listener 监听器
     */
    void registryMessageListener(String topic, MessageListener listener);
}
