package com.kfyty.loveqq.framework.core.lang;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.NestedConfigurationProperty;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 描述: 包装一个值
 *
 * @author kfyty725
 * @date 2021/9/19 11:08
 * @email kfyty725@hotmail.com
 */
@Data
@NestedConfigurationProperty
public class Value<V> implements Serializable {
    /**
     * value
     */
    private V value;

    public Value() {

    }

    public Value(V value) {
        this.value = value;
    }

    public V get() {
        return this.value;
    }

    public void set(V value) {
        this.setValue(value);
    }

    @Override
    public int hashCode() {
        return CommonUtil.hashCode(this.value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Value<?> && Objects.deepEquals(this.value, ((Value<?>) obj).value);
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
