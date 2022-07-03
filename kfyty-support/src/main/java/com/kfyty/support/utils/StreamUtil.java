package com.kfyty.support.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.function.BinaryOperator;

/**
 * 描述:
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
}
