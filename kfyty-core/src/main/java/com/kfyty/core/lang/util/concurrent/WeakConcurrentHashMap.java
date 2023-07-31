package com.kfyty.core.lang.util.concurrent;

/**
 * 描述: 弱引用
 *
 * @author kfyty725
 * @date 2023/7/31 18:10
 * @email kfyty725@hotmail.com
 */
public class WeakConcurrentHashMap<K, V> extends ReferenceConcurrentHashMap<K, V> {

    public WeakConcurrentHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, ReferenceType.WEAK);
    }

    public WeakConcurrentHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, ReferenceType.WEAK);
    }

    public WeakConcurrentHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, DEFAULT_CONCURRENCY_LEVEL, ReferenceType.WEAK);
    }

    public WeakConcurrentHashMap(int initialCapacity, int concurrencyLevel) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, concurrencyLevel, ReferenceType.WEAK);
    }

    public WeakConcurrentHashMap(int initialCapacity, ReferenceType referenceType) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, referenceType);
    }

    public WeakConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        this(initialCapacity, loadFactor, concurrencyLevel, ReferenceType.WEAK);
    }

    public WeakConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel, ReferenceType referenceType) {
        super(initialCapacity, loadFactor, concurrencyLevel, referenceType);
    }
}
