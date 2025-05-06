package com.kfyty.loveqq.framework.core.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;

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
     * 布尔值转换为字节缓冲
     *
     * @param _boolean_ 布尔值
     * @return 字节缓冲
     */
    public static ByteBuf from(Boolean _boolean_) {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeBoolean(_boolean_);
        return buffer;
    }

    /**
     * 数字转换为字节缓冲
     *
     * @param number 数字
     * @return 字节缓冲
     */
    public static ByteBuf from(Number number) {
        ByteBuf buffer = Unpooled.buffer();
        if (number instanceof Float || number instanceof Double || number instanceof BigDecimal) {
            buffer.writeDouble(number.doubleValue());
        } else if (number instanceof Long || number instanceof BigInteger) {
            buffer.writeLong(number.longValue());
        } else {
            buffer.writeInt(number.intValue());
        }
        return buffer;
    }

    /**
     * 字节数组转换为字节缓冲
     *
     * @param bytes 字节数组
     * @return 字节缓冲
     */
    public static ByteBuf from(byte[] bytes) {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeBytes(bytes);
        return buffer;
    }
}
