package com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig.mq;

/**
 * 描述: rocketmq 消息监听器模式
 *
 * @author kfyty725
 * @date 2024/12/09 21:54
 * @email kfyty725@hotmail.com
 */
public enum MessageConsumeMode {
    /**
     * 任意一个消费
     */
    ANY,

    /**
     * 全部监听器都消费
     */
    ALL;
}
