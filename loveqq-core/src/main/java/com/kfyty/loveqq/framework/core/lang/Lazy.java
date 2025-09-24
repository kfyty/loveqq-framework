package com.kfyty.loveqq.framework.core.lang;

import com.kfyty.loveqq.framework.core.autoconfig.LaziedObject;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 描述: 懒加载获取
 * 该类不可用于 {@link java.util.Map} 的 key
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
     * 返回是否创建过了实例
     *
     * @return true is created
     */
    public boolean isCreated() {
        return this.value != null;
    }

    @Override
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
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Lazy<?>)) {
            return false;
        }
        Lazy<?> other = (Lazy<?>) obj;
        if (this.isCreated() && other.isCreated()) {
            return Objects.equals(this.value, other.value);
        }
        return Objects.equals(this.provider, other.provider);
    }

    @Override
    public String toString() {
        return this.value == null ? "Not init: " + this.provider : this.value.toString();
    }

    /**
     * 工厂方法
     *
     * @param provider 提供者
     * @return 懒加载对象
     */
    public static <T> Lazy<T> of(Supplier<T> provider) {
        return new Lazy<>(provider);
    }
}
