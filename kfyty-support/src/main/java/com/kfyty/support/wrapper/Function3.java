package com.kfyty.support.wrapper;

/**
 * 描述: 支持三个参数的函数
 *
 * @author kfyty725
 * @date 2022/1/28 19:38
 * @email kfyty725@hotmail.com
 */
@FunctionalInterface
public interface Function3<R, T1, T2, T3> {
    R apply(T1 t1, T2 t2, T3 t3);
}
