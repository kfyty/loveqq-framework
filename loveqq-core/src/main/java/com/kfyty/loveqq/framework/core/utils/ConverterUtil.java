package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.converter.Converter;
import com.kfyty.loveqq.framework.core.support.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:58
 * @email kfyty725@hotmail.com
 */
public abstract class ConverterUtil {
    /**
     * 数据转换器
     */
    private static final Map<Pair<Class<?>, Class<?>>, Converter<?, ?>> TYPE_CONVERTER = new ConcurrentHashMap<>();

    /**
     * 初始化默认的转换器
     */
    static {
        PackageUtil.scanInstance(Converter.class).forEach(ConverterUtil::registryConverter);
    }

    public static Map<Pair<Class<?>, Class<?>>, Converter<?, ?>> getTypeConverters() {
        return TYPE_CONVERTER;
    }

    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    public static <S, T> Converter<S, T> getTypeConverter(Class<S> source, Class<T> target) {
        if (target != null && target.isEnum()) {
            return (Converter<S, T>) TYPE_CONVERTER.get(new Pair<>(source, Enum.class));
        }
        return (Converter<S, T>) TYPE_CONVERTER.get(new Pair<>(source, target));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <S, T> void registryConverter(Converter<S, T> converter) {
        Class<? extends Converter> converterClass = converter.getClass();
        registryConverter((Class<S>) ReflectUtil.getSuperGeneric(converterClass), (Class<T>) ReflectUtil.getSuperGeneric(converterClass, 1), converter);
    }

    public static <S, T> void registryConverter(Class<S> source, Class<T> target, Converter<S, ? extends T> converter) {
        TYPE_CONVERTER.put(new Pair<>(source, target), converter);
    }

    @SuppressWarnings("unchecked")
    public static <S, T> T convert(S source, Class<T> clazz) {
        Converter<S, T> converter = getTypeConverter((Class<S>) source.getClass(), clazz);
        if (converter == null) {
            throw new IllegalArgumentException("There's no suitable converter is available of type: " + source.getClass() + ", " + clazz);
        }
        if (clazz.isEnum() && String.valueOf(source).indexOf('.') < 0) {
            return converter.apply((S) (clazz.getName() + '.' + source));
        }
        return converter.apply(source);
    }
}
