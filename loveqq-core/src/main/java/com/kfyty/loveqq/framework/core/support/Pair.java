package com.kfyty.loveqq.framework.core.support;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.NestedConfigurationProperty;
import com.kfyty.loveqq.framework.core.lang.Value;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 描述: 一对值
 *
 * @author kfyty725
 * @date 2022/7/17 18:46
 * @email kfyty725@hotmail.com
 */
@Data
@NestedConfigurationProperty
@EqualsAndHashCode(callSuper = true)
public class Pair<K, V> extends Value<V> {
    /**
     * key
     */
    private K key;

    public Pair() {
        super();
    }

    public Pair(final K key, final V value) {
        super(value);
        this.key = key;
    }

    public void setKeyValue(final K key, final V value) {
        this.setKey(key);
        this.setValue(value);
    }

    @Override
    public String toString() {
        return "key=" + getKey() + ", value=" + getValue();
    }
}
