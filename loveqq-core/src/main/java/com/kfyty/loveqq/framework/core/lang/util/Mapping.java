package com.kfyty.loveqq.framework.core.lang.util;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 描述: 值映射工具类
 *
 * @author kfyty725
 * @date 2024/8/14 22:08
 * @email kfyty725@hotmail.com
 */
@AllArgsConstructor
@RequiredArgsConstructor
public class Mapping<T> implements Cloneable, Serializable {
    /**
     * 空值
     */
    public static final Mapping<?> NULLABLE = new Mapping<>(null);

    /**
     * 需要映射的值
     */
    private final T value;

    /**
     * 前一个值，用于 map 后的回退
     */
    private Mapping<?> prev;

    /**
     * 是否存在
     *
     * @return true if exists
     */
    public boolean exists() {
        return this.value != null;
    }

    /**
     * 获取当前值
     *
     * @return value
     */
    public T get() {
        return this.value;
    }

    /**
     * 获取当前值
     *
     * @param defaultValue 默认值
     * @return value
     */
    public T getOr(T defaultValue) {
        return this.value == null ? defaultValue : this.value;
    }

    /**
     * 获取当前值
     *
     * @param defaultValue 默认值
     * @return value
     */
    public T getOr(Supplier<T> defaultValue) {
        return this.value == null ? defaultValue.get() : this.value;
    }

    /**
     * 获取当前值
     *
     * @param throwable 异常
     * @return value
     */
    public <EX extends Throwable> T getThrow(EX throwable) throws EX {
        if (this.value == null) {
            throw throwable;
        }
        return this.value;
    }

    /**
     * 获取当前值
     *
     * @param throwable 异常
     * @return value
     */
    public <EX extends Throwable> T getThrow(Supplier<EX> throwable) throws EX {
        if (this.value == null) {
            throw throwable.get();
        }
        return this.value;
    }

    /**
     * 获取当前值
     *
     * @param parameter           参数
     * @param defaultValueMapping 默认值映射
     * @return value
     */
    public <P> T getOr(P parameter, Function<P, T> defaultValueMapping) {
        return this.value != null ? this.value : (parameter == null ? null : defaultValueMapping.apply(parameter));
    }

    /**
     * 当前值映射为另一个值
     *
     * @param mapping 映射关系
     * @return {@link this}
     */
    public <R> Mapping<R> map(Function<T, R> mapping) {
        return from(mapping.apply(this.value), this);
    }

    /**
     * 当前值扁平化映射为另一个值
     *
     * @param mapping 映射关系
     * @return {@link this}
     */
    public <R> Mapping<R> flatMap(Function<T, Mapping<R>> mapping) {
        Mapping<R> mapped = mapping.apply(this.value);
        mapped.prev = this;
        return mapped;
    }

    /**
     * 回退到上一步的值
     *
     * @param backType 用于强转泛型
     * @return this.prev
     */
    @SuppressWarnings("unchecked")
    public <B> Mapping<B> back(Class<B> backType) {
        return (Mapping<B>) this.prev;
    }

    /**
     * 消费当前值
     *
     * @param consumer 消费逻辑
     * @return this
     */
    public Mapping<T> then(Consumer<T> consumer) {
        if (this.value != null) {
            consumer.accept(this.value);
        }
        return this;
    }

    /**
     * 测试当前值，不符合置为空值
     *
     * @param test 断言逻辑
     * @return this
     */
    public Mapping<T> filter(Predicate<T> test) {
        if (test.test(this.value)) {
            return this;
        }
        return from(null);
    }

    /**
     * 测试当前值，符合时执行回调
     *
     * @param test 断言逻辑
     * @return this
     */
    public Mapping<T> when(Predicate<T> test, Consumer<T> consumer) {
        if (test.test(this.value)) {
            return then(consumer);
        }
        return from(null);
    }

    /**
     * 测试当前值，符合时执行映射
     *
     * @param test 断言逻辑
     * @return this
     */
    public <R> Mapping<R> when(Predicate<T> test, Function<T, R> mapping) {
        if (test.test(this.value)) {
            return map(mapping);
        }
        return from(null);
    }

    /**
     * 非空时当前值扁平化映射为另一个值
     *
     * @param mapping 映射关系
     * @return {@link this}
     */
    public <R> Mapping<R> notNullMap(Function<T, R> mapping) {
        if (this.value == null) {
            return from(null);
        }
        return map(mapping);
    }

    /**
     * 非空时当前值扁平化映射为另一个值
     *
     * @param mapping 映射关系
     * @return {@link this}
     */
    public <R> Mapping<R> notEmptyMap(Function<T, R> mapping) {
        if (CommonUtil.empty(this.value)) {
            return from(null);
        }
        return map(mapping);
    }

    /**
     * 非空时当前值扁平化映射为另一个值
     *
     * @param mapping 映射关系
     * @return {@link this}
     */
    public <R> Mapping<R> notNullFlatMap(Function<T, Mapping<R>> mapping) {
        if (this.value == null) {
            return from(null);
        }
        return flatMap(mapping);
    }

    /**
     * 非空时当前值扁平化映射为另一个值
     *
     * @param mapping 映射关系
     * @return {@link this}
     */
    public <R> Mapping<R> notEmptyFlatMap(Function<T, Mapping<R>> mapping) {
        if (CommonUtil.empty(this.value)) {
            return from(null);
        }
        return flatMap(mapping);
    }

    /**
     * 空值时执行逻辑
     *
     * @param runnable 执行的逻辑
     * @return this
     */
    public Mapping<T> whenNull(Runnable runnable) {
        if (this.value == null) {
            runnable.run();
        }
        return this;
    }

    /**
     * 空值时执行逻辑
     *
     * @param runnable 执行的逻辑
     * @return this
     */
    public Mapping<T> whenEmpty(Runnable runnable) {
        if (CommonUtil.empty(this.value)) {
            runnable.run();
        }
        return this;
    }

    /**
     * 非空时消费当前值
     *
     * @param consumer 消费逻辑
     * @return this
     */
    public Mapping<T> whenNotNull(Consumer<T> consumer) {
        return when(Objects::nonNull, consumer);
    }

    /**
     * 非空时消费当前值
     *
     * @param consumer 消费逻辑
     * @return this
     */
    public Mapping<T> whenNotEmpty(Consumer<T> consumer) {
        return when(CommonUtil::notEmpty, consumer);
    }

    /**
     * 转换为 {@link Optional}
     *
     * @return {@link Optional}
     */
    public Optional<T> optional() {
        return Optional.ofNullable(this.value);
    }

    @Override
    public Mapping<T> clone() {
        if (this == NULLABLE) {
            return this;
        }
        return from(this.value, this.prev == null ? null : this.prev.clone());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Mapping<?>)) {
            return false;
        }
        return Objects.equals(this.value, ((Mapping<?>) obj).value);
    }

    @Override
    public int hashCode() {
        return this.value == null ? 0 : this.value.hashCode();
    }

    @Override
    public String toString() {
        return this.value == null ? "null" : this.value.toString();
    }

    /**
     * 工厂方法
     *
     * @param value 值
     * @return {@link Mapping}
     */
    @SuppressWarnings("unchecked")
    public static <T> Mapping<T> from(T value) {
        if (value == null) {
            return (Mapping<T>) NULLABLE;
        }
        return new Mapping<>(value);
    }

    /**
     * 工厂方法
     *
     * @param value 值
     * @return {@link Mapping}
     */
    public static <T> Mapping<T> build(Mapping<T> value) {
        return from(value.get());
    }

    /**
     * 工厂方法
     *
     * @param value 值
     * @return {@link Mapping}
     */
    public static <T> Mapping<T> build(Optional<T> value) {
        return from(value.orElse(null));
    }

    /**
     * 工厂方法
     *
     * @param value 值
     * @param prev  前一个值
     * @return {@link Mapping}
     */
    public static <T> Mapping<T> from(T value, Mapping<?> prev) {
        return new Mapping<>(value, prev);
    }

    /**
     * 工厂方法
     *
     * @param value 值
     * @param prev  前一个值
     * @return {@link Mapping}
     */
    public static <T> Mapping<T> build(Mapping<T> value, Mapping<?> prev) {
        return from(value.get(), prev);
    }

    /**
     * 工厂方法
     *
     * @param value 值
     * @param prev  前一个值
     * @return {@link Mapping}
     */
    public static <T> Mapping<T> build(Optional<T> value, Mapping<?> prev) {
        return from(value.orElse(null), prev);
    }
}
