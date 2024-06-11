package com.kfyty.loveqq.framework.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 描述: stream 工具
 *
 * @author kfyty725
 * @date 2022/7/3 17:43
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class StreamUtil {

    public static <U> BinaryOperator<U> logMergeFunction() {
        return (v1, v2) -> {
            log.warn("stream to map merge conflict: {} <-> {}, use: {}", v1, v2, v1);
            return v1;
        };
    }

    public static <U> BinaryOperator<U> throwMergeFunction() {
        return (v1, v2) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", v1));
        };
    }

    /**
     * 分组映射并扁平化
     */
    @SuppressWarnings("unchecked")
    public static <T, Nested, U extends Collection<Nested>, R> Collector<T, ?, R> flatMap(Function<? super T, ? extends U> mapper) {
        return (Collector<T, ?, R>) flatMap(mapper, Collectors.toSet());
    }

    /**
     * 分组映射并扁平化
     */
    public static <T, Nested, U extends Collection<Nested>, A, R> Collector<T, ?, R> flatMap(Function<? super T, ? extends U> mapper, Collector<? super Nested, A, R> downstream) {
        return Collectors.mapping(mapper, Collectors.collectingAndThen(Collectors.toList(), e -> e.stream().flatMap(Collection::stream).collect(downstream)));
    }
}
