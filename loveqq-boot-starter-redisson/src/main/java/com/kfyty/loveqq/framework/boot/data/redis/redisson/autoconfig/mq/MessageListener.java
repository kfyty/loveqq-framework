package com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig.mq;

/**
 * 描述: redis 消息监听器
 *
 * @author kfyty725
 * @date 2022/5/31 20:49
 * @email kfyty725@hotmail.com
 */
public interface MessageListener {
    /**
     * 消息回调
     *
     * @param message 消息
     */
    void onMessage(Object message);
}
