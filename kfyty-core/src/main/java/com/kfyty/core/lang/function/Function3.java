package com.kfyty.core.lang.function;

/**
 * 描述: 支持三个参数的函数
 *
 * @author kfyty725
 * @date 2022/1/28 19:38
 * @email kfyty725@hotmail.com
 */
@FunctionalInterface
public interface Function3<T1, T2, T3, R> {
    R apply(T1 t1, T2 t2, T3 t3);
}
