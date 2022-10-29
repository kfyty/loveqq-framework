package com.kfyty.core.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 描述: 一对值
 *
 * @author kfyty725
 * @date 2022/7/17 18:46
 * @email kfyty725@hotmail.com
 */
@Data
@AllArgsConstructor
public class Pair<K, V> {
    /**
     * key
     */
    private K key;

    /**
     * value
     */
    private V value;
}
