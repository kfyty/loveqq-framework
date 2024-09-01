package com.kfyty.loveqq.framework.core.lang.util;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 描述: 值映射工具类
 *
 * @author kfyty725
 * @date 2024/8/14 22:08
 * @email kfyty725@hotmail.com
 */
@AllArgsConstructor
@RequiredArgsConstructor
public class Mapping<T> {
    /**
     * 需要映射的值
     */
    private final T value;

    /**
     * 前一个值，用于 map 后的回退
     */
    private Mapping<?> prev;

    /**
     * 获取当前值
     *
     * @return value
     */
    public T get() {
        return this.value;
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
        return (Mapping<B>) (this.prev == null ? this : this.prev);
    }

    /**
     * 消费当前值
     *
     * @param consumer 消费逻辑
     * @return this
     */
    public Mapping<T> to(Consumer<T> consumer) {
        consumer.accept(this.value);
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
            return to(consumer);
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
     * 工厂方法
     *
     * @param value 值
     * @return {@link Mapping}
     */
    public static <T> Mapping<T> from(T value) {
        return new Mapping<>(value);
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
}
