package com.kfyty.loveqq.framework.boot.mq.rocket.autoconfig.annotation;

import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.MessageListener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: rocketmq 消息监听器，通过该监听器监听的都是 {@link org.apache.rocketmq.client.apis.consumer.PushConsumer}
 * <p>
 * 作用在类上时，必须是 {@link MessageListener} 的实现类
 * 作用在方法上时，该方法将作为回调方法，并且类上也必须注解有 {@link RocketMQMessageListener} 才有效
 *
 * @author kfyty725
 * @date 2024/12/09 21:54
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RocketMQMessageListener {
    /**
     * 订阅的话题
     */
    String value() default "";

    /**
     * {@link org.apache.rocketmq.client.apis.consumer.PushConsumerBuilder#setConsumerGroup(String)}
     */
    String consumerGroup() default "";

    /**
     * {@link FilterExpressionType}
     */
    FilterExpressionType filterType() default FilterExpressionType.TAG;

    /**
     * {@link org.apache.rocketmq.client.apis.consumer.FilterExpression}
     */
    String filterExpress() default "*";

    /**
     * {@link org.apache.rocketmq.client.apis.consumer.PushConsumerBuilder#setMaxCacheMessageCount(int)}
     */
    int maxCacheMessageCount() default -1;

    /**
     * {@link org.apache.rocketmq.client.apis.consumer.PushConsumerBuilder#setMaxCacheMessageSizeInBytes(int)}
     */
    int maxCacheMessageSizeInBytes() default -1;

    /**
     * 每个 {@link RocketMQMessageListener} 注解都会生成一个 {@link org.apache.rocketmq.client.apis.consumer.PushConsumer} 实例，并创建线程池
     * {@link org.apache.rocketmq.client.apis.consumer.PushConsumerBuilder#setConsumptionThreadCount(int)}
     */
    int consumptionThreadCount() default 4;
}
