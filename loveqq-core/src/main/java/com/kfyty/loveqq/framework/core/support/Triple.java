package com.kfyty.loveqq.framework.core.support;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.NestedConfigurationProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 描述: 三个值
 *
 * @author kfyty725
 * @date 2022/7/17 18:46
 * @email kfyty725@hotmail.com
 */
@Data
@NestedConfigurationProperty
@EqualsAndHashCode(callSuper = true)
public class Triple<K, V, T> extends Pair<K, V> {
    /**
     * tripe
     */
    private T triple;

    public Triple() {
        super();
    }

    public Triple(K key, V value, T triple) {
        super(key, value);
        this.triple = triple;
    }

    @Override
    public String toString() {
        return "key=" + getKey() + ", value=" + getValue() + "triple=" + this.getTriple();
    }
}
