package com.kfyty.loveqq.framework.web.mvc.reactor.ws;

import com.kfyty.loveqq.framework.core.utils.NIOUtil;
import io.netty.buffer.ByteBuf;

/**
 * 描述: websocket 处理器
 *
 * @author kfyty725
 * @date 2024/7/29 20:26
 * @email kfyty725@hotmail.com
 */
public interface WebSocketHandler {
    /**
     * 返回支持的处理端点，也即请求路径
     *
     * @return path point
     */
    String getEndPoint();

    /**
     * websocket 连接回调
     *
     * @param session 连接会话
     */
    void onOpen(Session session);

    /**
     * websocket 消息回调
     *
     * @param session 连接会话
     * @param bytes   消息体
     */
    void onMessage(Session session, byte[] bytes);

    /**
     * websocket 消息回调
     *
     * @param session 连接会话
     * @param byteBuf 消息体
     */
    default void onMessage(Session session, ByteBuf byteBuf) {
        this.onMessage(session, NIOUtil.read(byteBuf));
    }

    /**
     * websocket 异常回调
     *
     * @param session   连接会话
     * @param throwable 异常
     */
    void onError(Session session, Throwable throwable);

    /**
     * websocket 关闭会话回调
     *
     * @param session 连接会话
     */
    void onClose(Session session);
}
