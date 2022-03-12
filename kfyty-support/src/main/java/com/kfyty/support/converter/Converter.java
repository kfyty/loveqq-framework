package com.kfyty.support.converter;

/**
 * 描述: 数据类型转换
 *
 * @author kfyty725
 * @date 2022/3/12 12:43
 * @email kfyty725@hotmail.com
 */
public interface Converter<S, T> {
    /**
     * 将 S 转换为 T
     *
     * @param source 源数据
     * @return 转换后的数据
     */
    T apply(S source);
}
