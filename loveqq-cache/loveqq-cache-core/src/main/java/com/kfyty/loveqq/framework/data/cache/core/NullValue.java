package com.kfyty.loveqq.framework.data.cache.core;

/**
 * 描述: 缓存空值表示
 *
 * @author kfyty725
 * @date 2024/7/4 9:59
 * @email kfyty725@hotmail.com
 */
public final class NullValue {
    public static final Object INSTANCE = new NullValue();

    private NullValue() {
    }

    private Object readResolve() {
        return INSTANCE;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj == null;
    }

    @Override
    public int hashCode() {
        return NullValue.class.hashCode();
    }

    @Override
    public String toString() {
        return "null";
    }
}
