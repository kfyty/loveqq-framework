package com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig.mq.annotation;

import com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig.mq.MessageConsumeMode;
import com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig.mq.RedisMessageQueue;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: redis mq 消费者，只有同时注解在类上才有效
 *
 * @author kfyty725
 * @date 2022/5/31 14:49
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RedisMQMessageConsumer {
    /**
     * topic
     *
     * @return topic
     */
    String value() default RedisMessageQueue.DEFAULT_QUEUE;

    /**
     * 消费模式
     * <p>
     * 如果有若干个消费者，且设置了不同的消费模式，则所有模式为 {@link MessageConsumeMode#ALL} 的全部消费，所有模式为 {@link MessageConsumeMode#ANY} 的仅有一个消费
     * 如果有仅有两个消费者，一个 {@link MessageConsumeMode#ALL}，一个 {@link MessageConsumeMode#ANY}，则两个都会消费
     *
     * @return 消费模式
     */
    MessageConsumeMode mode() default MessageConsumeMode.ALL;
}
