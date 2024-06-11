package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.lang.function.SerializableFunction;
import com.kfyty.loveqq.framework.core.lang.util.concurrent.WeakConcurrentHashMap;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Map;

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
    private static final Map<Class<?>, SerializedLambda> SERIALIZED_LAMBDA_CACHE = new WeakConcurrentHashMap<>();

    public static <T> String resolveFieldName(SerializableFunction<T, ?> serializableFunction) {
        String implMethodName = serializeLambda(serializableFunction).getImplMethodName();
        if (implMethodName.length() < 4) {
            return implMethodName.replace("get", "");
        }
        return Character.toLowerCase(implMethodName.charAt(3)) + implMethodName.substring(4);
    }

    public static <T> SerializedLambda serializeLambda(SerializableFunction<T, ?> serializableFunction) {
        final Class<?> clazz = serializableFunction.getClass();
        return ofNullable(SERIALIZED_LAMBDA_CACHE.get(clazz))
                .orElseGet(() -> {
                    Method method = ReflectUtil.getMethod(clazz, "writeReplace");
                    SerializedLambda serializedLambda = (SerializedLambda) ReflectUtil.invokeMethod(serializableFunction, method);
                    SERIALIZED_LAMBDA_CACHE.put(clazz, serializedLambda);
                    return serializedLambda;
                });
    }
}
