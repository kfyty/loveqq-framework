package com.kfyty.loveqq.framework.core.support;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 描述: 三个值
 *
 * @author kfyty725
 * @date 2022/7/17 18:46
 * @email kfyty725@hotmail.com
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Triple<K, V, T> extends Pair<K, V> {
    private T triple;

    public Triple(K key, V value, T triple) {
        super(key, value);
        this.triple = triple;
    }
}
