package com.kfyty.support.utils;

import com.kfyty.support.wrapper.SerializableFunction;
import com.kfyty.support.wrapper.WeakKey;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import static java.util.Optional.ofNullable;

/**
 * 描述: lambda 序列化工具
 *
 * @author kfyty725
 * @date 2021/9/17 20:38
 * @email kfyty725@hotmail.com
 */
public abstract class SerializableLambdaUtil {
    /**
     * SerializedLambda 缓存
     */
    private static final Map<WeakKey<Class<?>>, SerializedLambda> SERIALIZED_LAMBDA_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    public static <T> String resolveFieldName(SerializableFunction<T, ?> serializableFunction) {
        String implMethodName = serializeLambda(serializableFunction).getImplMethodName();
        if (implMethodName.length() < 4) {
            return implMethodName.replace("get", "");
        }
        return Character.toLowerCase(implMethodName.charAt(3)) + implMethodName.substring(4);
    }

    public static <T> SerializedLambda serializeLambda(SerializableFunction<T, ?> serializableFunction) {
        final Class<?> clazz = serializableFunction.getClass();
        final WeakKey<Class<?>> key = new WeakKey<>(clazz);
        return ofNullable(SERIALIZED_LAMBDA_CACHE.get(key))
                .orElseGet(() -> {
                    Method method = ReflectUtil.getMethod(clazz, "writeReplace");
                    SerializedLambda serializedLambda = (SerializedLambda) ReflectUtil.invokeMethod(serializableFunction, method);
                    SERIALIZED_LAMBDA_CACHE.put(key, serializedLambda);
                    return serializedLambda;
                });
    }
}
