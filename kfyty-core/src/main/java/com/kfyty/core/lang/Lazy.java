package com.kfyty.core.lang;

import com.kfyty.core.autoconfig.LaziedObject;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 描述: 懒加载获取
 *
 * @author kfyty725
 * @date 2021/9/19 11:08
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class Lazy<T> implements LaziedObject<T> {
    /**
     * 值提供者
     */
    private final Supplier<T> provider;

    /**
     * 实际值
     */
    private volatile T value;

    /**
     * 创建新的值
     *
     * @return value
     */
    public T create() {
        return this.provider.get();
    }

    @Override
    public T get() {
        if (this.value == null) {
            synchronized (this) {
                if (this.value == null) {
                    this.value = this.create();
                }
            }
        }
        return this.value;
    }

    @Override
    public int hashCode() {
        return this.value == null ? this.provider.hashCode() : this.value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Lazy && Objects.equals(this.provider, ((Lazy<?>) obj).provider);
    }

    @Override
    public String toString() {
        return this.value == null ? "not init: " + this.provider : this.value.toString();
    }
}
