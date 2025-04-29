package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.lang.util.concurrent.WeakConcurrentHashMap;
import com.kfyty.loveqq.framework.core.support.Pair;
import lombok.SneakyThrows;

import java.io.Serializable;
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

    /**
     * 从 lambda 表达式获取字段名称
     *
     * @param serializableFunction 实现了 {@link Serializable} 接口的 lambda 表达式
     * @return 字段名称
     */
    public static String resolveFieldName(Serializable serializableFunction) {
        String implMethodName = serializeLambda(serializableFunction).getImplMethodName();
        if (implMethodName.length() < 4) {
            return implMethodName.replace("get", "");
        }
        return Character.toLowerCase(implMethodName.charAt(3)) + implMethodName.substring(4);
    }

    /**
     * 从 lambda 表达式获取方法
     *
     * @param serializableFunction 实现了 {@link Serializable} 接口的 lambda 表达式
     * @return 实例及方法对象
     */
    @SneakyThrows({NoSuchMethodException.class, SecurityException.class})
    public static Pair<Object, Method> resolveMethod(Serializable serializableFunction, Class<?>... paramTypes) {
        SerializedLambda serializedLambda = serializeLambda(serializableFunction);
        Class<?> clazz = ReflectUtil.load(serializedLambda.getImplClass().replace('/', '.'));
        String methodName = serializedLambda.getImplMethodName();
        Method method = ReflectUtil.getMethod(clazz, methodName, paramTypes);
        if (method == null) {
            method = clazz.getDeclaredMethod(methodName, paramTypes);
        }
        if (serializedLambda.getCapturedArgCount() < 1) {
            return new Pair<>(null, method);
        }
        return new Pair<>(serializedLambda.getCapturedArg(0), method);
    }

    /**
     * 序列化 lambda
     *
     * @param serializableFunction 实现了 {@link Serializable} 接口的 lambda 表达式
     * @return 序列化的 lambda
     */
    public static SerializedLambda serializeLambda(Serializable serializableFunction) {
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
