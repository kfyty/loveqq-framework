package com.kfyty.loveqq.framework.core.lang;

import com.kfyty.loveqq.framework.core.autoconfig.LaziedObject;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * 描述: 懒加载获取
 * 该类不可用于 {@link java.util.Map} 的 key
 *
 * @author kfyty725
 * @date 2021/9/19 11:08
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Lazy<T> implements LaziedObject<T> {
    /**
     * 值提供者
     */
    private final ThrowableSupplier<T> provider;

    /**
     * 构建 {@link #provider} 的参数
     * 主要用于 {@link #equals(Object)}，判断是否是相同的懒加载对象
     */
    private final Object[] arguments;

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
        try {
            return this.provider.get();
        } catch (Throwable e) {
            throw new ResolvableException("Create lazy target instance failed.", e);
        }
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
        return Objects.deepEquals(this.arguments, other.arguments);
    }

    @Override
    public String toString() {
        return this.value == null ? "Not init: " + this.provider : this.value.toString();
    }

    /**
     * 可抛出异常的提供者
     *
     * @param <T>
     */
    public interface ThrowableSupplier<T> {
        /**
         * 获取值
         *
         * @return 值
         */
        T get() throws Throwable;
    }

    /**
     * 工厂方法
     *
     * @param provider 提供者
     * @param args     构建 provider 的参数
     * @return 懒加载对象
     */
    public static <T> Lazy<T> of(ThrowableSupplier<T> provider, Object... args) {
        return new Lazy<>(provider, args);
    }
}
