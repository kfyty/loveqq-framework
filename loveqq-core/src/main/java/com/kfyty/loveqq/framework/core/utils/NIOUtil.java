package com.kfyty.loveqq.framework.core.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 描述: nio 工具
 *
 * @author kfyty725
 * @date 2022/7/2 11:13
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class NIOUtil {
    /**
     * 读取输入流到字节数组
     *
     * @param byteBuffer 输入缓冲
     * @return 字节数组
     */
    public static byte[] read(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes, 0, bytes.length);
        return bytes;
    }

    /**
     * 读取输入流到字节数组
     *
     * @param byteBuf 输入缓冲
     * @return 字节数组
     */
    public static byte[] read(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes, 0, bytes.length);
        return bytes;
    }

    /**
     * 创建一个 {@link ByteBuf}
     *
     * @param bytes 字节数据
     * @return {@link ByteBuf}
     */
    public static ByteBuf newByteBuf(byte[] bytes) {
        return Unpooled.wrappedBuffer(bytes);
    }

    /**
     * 格式化 sse 数据
     *
     * @param data 实际数据
     * @return 符合 sse 标准的数据
     */
    public static ByteBuf formatSseData(Object data) {
        if (data instanceof CharSequence) {
            return newByteBuf(("data:" + data + "\n\n").getBytes(StandardCharsets.UTF_8));
        }
        if (data instanceof byte[]) {
            ByteBuf buffer = Unpooled.buffer();
            buffer.writeBytes("data:".getBytes(StandardCharsets.UTF_8));
            buffer.writeBytes((byte[]) data);
            buffer.writeBytes("\n\n".getBytes(StandardCharsets.UTF_8));
            return buffer;
        }
        throw new IllegalArgumentException("The sse value must be String/byte[]");
    }
}
