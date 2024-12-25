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
}
