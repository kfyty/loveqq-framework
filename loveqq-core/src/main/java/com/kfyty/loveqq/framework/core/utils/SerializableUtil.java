package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Objects;

/**
 * 描述: 序列化工具
 *
 * @author kfyty725
 * @date 2021/7/19 10:45
 * @email kfyty725@hotmail.com
 */
public abstract class SerializableUtil {

    public static byte[] serialize(Object o) {
        Objects.requireNonNull(o);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(ConstantConfig.IO_STREAM_READ_BUFFER_SIZE);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(o);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new ResolvableException("serialize object failed: " + o, e);
        }
    }

    public static Object deserialize(byte[] bytes) {
        Objects.requireNonNull(bytes);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return objectInputStream.readObject();
        } catch (Exception e) {
            throw new ResolvableException("deserialize object failed.", e);
        }
    }

    public static <T> T clone(T source) {
        return clone(source, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T clone(T source, Map<T, byte[]> byteCache) {
        Objects.requireNonNull(source);
        if (byteCache == null) {
            return (T) deserialize(serialize(source));
        }
        return (T) deserialize(byteCache.computeIfAbsent(source, k -> serialize(source)));
    }
}
