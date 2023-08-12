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
        super();
    }

    public WeakConcurrentHashMap(int initialCapacity) {
        super(ReferenceType.WEAK, initialCapacity);
    }

    public WeakConcurrentHashMap(int initialCapacity, float loadFactor) {
        super(ReferenceType.WEAK, initialCapacity, loadFactor);
    }
}
