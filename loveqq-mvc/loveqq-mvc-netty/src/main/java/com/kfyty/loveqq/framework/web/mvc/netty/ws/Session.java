package com.kfyty.loveqq.framework.web.mvc.netty.ws;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.InetSocketAddress;

/**
 * 描述: websocket session
 *
 * @author kfyty725
 * @date 2024/7/30 10:30
 * @email kfyty725@hotmail.com
 */
public interface Session {
    /**
     * 返回连接 uri
     *
     * @return uri
     */
    String uri();

    /**
     * 获取远程地址
     *
     * @return 远程地址
     */
    InetSocketAddress getRemoteAddress();

    /**
     * 获取连接请求
     *
     * @return 连接请求
     */
    ServerRequest getConnectRequest();

    /**
     * 连接是否存活
     *
     * @return true if active
     */
    boolean isActive();

    /**
     * 发送字符串
     *
     * @param message 消息体
     */
    Publisher<Void> sendStringAsync(Publisher<? extends String> message);

    /**
     * 发送文件
     *
     * @param file 文件
     */
    Publisher<Void> sendFileAsync(File file);

    /**
     * 发送字节数组
     *
     * @param bytes 字节数组
     */
    Publisher<Void> sendByteArrayAsync(Publisher<? extends byte[]> bytes);

    /**
     * 发送字节数组
     *
     * @param byteBuf 字节数组
     */
    Publisher<Void> sendByteBufAsync(Publisher<? extends ByteBuf> byteBuf);

    /**
     * 关闭 session
     */
    void close();

    /**
     * 发送字符串
     *
     * @param message 消息体
     */
    default void sendString(String message) {
        Mono.from(this.sendStringAsync(Mono.just(message))).subscribe();
    }

    /**
     * 发送文件
     *
     * @param file 文件
     */
    default void sendFile(File file) {
        Mono.from(this.sendFileAsync(file)).subscribe();
    }

    /**
     * 发送字节数组
     *
     * @param bytes 字节数组
     */
    default void sendByteArray(byte[] bytes) {
        Mono.from(this.sendByteArrayAsync(Mono.just(bytes))).subscribe();
    }

    /**
     * 发送字节数组
     *
     * @param byteBuf 字节数组
     */
    default void sendByteBuf(ByteBuf byteBuf) {
        this.sendByteBuf(byteBuf, true);
    }

    /**
     * 发送字节数组
     *
     * @param byteBuf 字节数组
     * @param release 是否自动 release
     */
    default void sendByteBuf(ByteBuf byteBuf, boolean release) {
        if (!release) {
            Mono.from(this.sendByteBufAsync(Mono.just(byteBuf))).subscribe();
            return;
        }
        Mono.from(this.sendByteBufAsync(Mono.just(byteBuf))).doFinally(s -> byteBuf.release()).subscribe();
    }
}
