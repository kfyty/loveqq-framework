package com.kfyty.loveqq.framework.web.mvc.reactor.ws;

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
     * 查询参数可从该方法返回值获取
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
     * 发送文件
     *
     * @param file 文件
     */
    Publisher<Void> sendFileAsync(File file);

    /**
     * 发送字符串
     *
     * @param message 消息体
     */
    Publisher<Void> sendStringAsync(Publisher<String> message);

    /**
     * 发送字节数组
     *
     * @param bytes 字节数组
     */
    Publisher<Void> sendByteArrayAsync(Publisher<byte[]> bytes);

    /**
     * 发送字节数组
     *
     * @param byteBuf 字节数组
     */
    Publisher<Void> sendAsync(Publisher<ByteBuf> byteBuf);

    /**
     * 关闭 session
     */
    Publisher<Void> closeAsync();

    /**
     * 关闭 session
     *
     * @param status 关闭状态码
     * @param reason 关闭原因
     */
    Publisher<Void> closeAsync(int status, String reason);

    /**
     * 发送文件
     *
     * @param file 文件
     */
    default void sendFile(File file) {
        Mono.from(this.sendFileAsync(file)).subscribe();
    }

    /**
     * 发送字符串
     *
     * @param message 消息体
     */
    default void sendString(String message) {
        Mono.from(this.sendStringAsync(Mono.just(message))).subscribe();
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
     * 发送后会自动释放 {@link ByteBuf}
     *
     * @param byteBuf 字节数组
     */
    default void send(ByteBuf byteBuf) {
        Mono.from(this.sendAsync(Mono.just(byteBuf))).subscribe();
    }

    /**
     * 关闭 session
     */
    default void close() {
        Mono.from(this.closeAsync()).subscribe();
    }

    /**
     * 关闭 session
     *
     * @param status 关闭状态码
     * @param reason 关闭原因
     */
    default void close(int status, String reason) {
        Mono.from(this.closeAsync(status, reason)).subscribe();
    }
}
