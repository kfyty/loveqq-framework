package com.kfyty.core.wrapper.function;

import java.io.Serializable;

/**
 * 描述: 可序列化的函数式接口
 *
 * @author kfyty725
 * @date 2021/9/17 20:37
 * @email kfyty725@hotmail.com
 */
@FunctionalInterface
public interface SerializableFunction<T, R> extends Serializable {
    R apply(T t);
}
