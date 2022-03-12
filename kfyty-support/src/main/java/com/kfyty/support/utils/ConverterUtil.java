package com.kfyty.support.utils;

import com.kfyty.support.converter.Converter;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;

import static com.kfyty.support.utils.ReflectUtil.getSuperGeneric;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:58
 * @email kfyty725@hotmail.com
 */
public abstract class ConverterUtil {
    private static final Map<Pair<Class<?>, Class<?>>, Converter<?, ?>> TYPE_CONVERTER = new HashMap<>();

    static {
        PackageUtil.scanInstance(Converter.class).forEach(ConverterUtil::registerConverter);
    }

    public static Map<Pair<Class<?>, Class<?>>, Converter<?, ?>> getTypeConverters() {
        return TYPE_CONVERTER;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public static Converter<?, ?> getTypeConverter(Class<?> source, Class<?> target) {
        return TYPE_CONVERTER.get(new Pair<>(source, target));
    }

    public static void registerConverter(Converter<?, ?> converter) {
        registerConverter(getSuperGeneric(converter.getClass()), getSuperGeneric(converter.getClass(), 1), converter);
    }

    public static void registerConverter(Class<?> source, Class<?> target, Converter<?, ?> converter) {
        TYPE_CONVERTER.put(new Pair<>(source, target), converter);
    }

    @SuppressWarnings("unchecked")
    public static <S, T> T convert(S source, Class<T> clazz) {
        Converter<?, ?> converter = getTypeConverter(source.getClass(), clazz);
        if (converter != null) {
            return ((Converter<S, T>) converter).apply(source);
        }
        throw new IllegalArgumentException("no corresponding converter is available");
    }
}
