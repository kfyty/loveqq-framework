package com.kfyty.loveqq.framework.core.lang.util.concurrent;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 描述: 引用同步 Map
 *
 * @author kfyty725
 * @date 2023/7/31 18:10
 * @email kfyty725@hotmail.com
 */
public class ReferenceConcurrentHashMap<K, V> implements ConcurrentMap<K, V>, Serializable {
    /**
     * 包装 Map
     */
    private final Map<Reference<K>, V> target;

    /**
     * 引用队列
     */
    private final ReferenceQueue<K> referenceQueue;

    /**
     * 引用类型
     */
    private final ReferenceType referenceType;

    /**
     * 是否正在清理中
     */
    private final AtomicBoolean purge;

    public ReferenceConcurrentHashMap() {
        this(ReferenceType.WEAK);
    }

    public ReferenceConcurrentHashMap(ReferenceType referenceType) {
        this(referenceType, 16);
    }

    public ReferenceConcurrentHashMap(ReferenceType referenceType, int initialCapacity) {
        this(referenceType, initialCapacity, 0.75F);
    }


    public ReferenceConcurrentHashMap(ReferenceType referenceType, int initialCapacity, float loadFactor) {
        this.target = new ConcurrentHashMap<>(initialCapacity, loadFactor);
        this.referenceQueue = new ReferenceQueue<>();
        this.referenceType = referenceType;
        this.purge = new AtomicBoolean(false);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        this.purgeKeys();
        return this.target.putIfAbsent(this.wrapKey(key, this.referenceQueue), value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object key, Object value) {
        this.purgeKeys();
        return this.target.remove(this.wrapKey((K) key, null), value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        this.purgeKeys();
        return this.target.replace(this.wrapKey(key, this.referenceQueue), oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        this.purgeKeys();
        return this.target.replace(this.wrapKey(key, this.referenceQueue), value);
    }

    @Override
    public int size() {
        this.purgeKeys();
        return this.target.size();
    }

    @Override
    public boolean isEmpty() {
        this.purgeKeys();
        return this.target.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(Object key) {
        this.purgeKeys();
        return this.target.containsKey(this.wrapKey((K) key, null));
    }

    @Override
    public boolean containsValue(Object value) {
        this.purgeKeys();
        return this.target.containsValue(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        this.purgeKeys();
        return this.target.get(this.wrapKey((K) key, null));
    }

    @Override
    public V put(K key, V value) {
        this.purgeKeys();
        return this.target.put(this.wrapKey(key, this.referenceQueue), value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        this.purgeKeys();
        return this.target.remove(this.wrapKey((K) key, null));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.purgeKeys();
        m.forEach((k, v) -> this.target.put(this.wrapKey(k, this.referenceQueue), v));
    }

    @Override
    public void clear() {
        this.purgeKeys();
        this.target.clear();
    }

    @Override
    public Set<K> keySet() {
        this.purgeKeys();
        return this.target.keySet().stream().filter(Objects::nonNull).map(Reference::get).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public Collection<V> values() {
        this.purgeKeys();
        return this.target.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        this.purgeKeys();
        return this.target.entrySet()
                .stream()
                .filter(e -> e.getKey() != null)
                .map(entry -> new AbstractMap.SimpleImmutableEntry<>(entry.getKey().get(), entry.getValue()))
                .filter(e -> e.getKey() != null)
                .collect(Collectors.toSet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getOrDefault(Object key, V defaultValue) {
        this.purgeKeys();
        return this.target.getOrDefault(this.wrapKey((K) key, null), defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        this.purgeKeys();
        this.target.forEach((k, v) -> action.accept(k.get(), v));
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        this.purgeKeys();
        this.target.replaceAll((k, v) -> function.apply(k.get(), v));
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        this.purgeKeys();
        return this.target.computeIfAbsent(this.wrapKey(key, this.referenceQueue), referenceKey -> mappingFunction.apply(key));
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        this.purgeKeys();
        return this.target.computeIfPresent(this.wrapKey(key, this.referenceQueue), (referenceKey, value) -> remappingFunction.apply(key, value));
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        this.purgeKeys();
        return this.target.compute(this.wrapKey(key, this.referenceQueue), (referenceKey, value) -> remappingFunction.apply(key, value));
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        this.purgeKeys();
        return this.target.merge(this.wrapKey(key, this.referenceQueue), value, remappingFunction);
    }

    /**
     * 包装 key
     *
     * @param key key
     * @return 引用 key
     */
    public Reference<K> wrapKey(K key, ReferenceQueue<K> referenceQueue) {
        switch (this.referenceType) {
            case SOFT:
                return new SoftKey<>(key, referenceQueue);
            case WEAK:
                return new WeakKey<>(key, referenceQueue);
        }
        throw new IllegalStateException(this.referenceType.toString());
    }

    /**
     * 清楚被回收的 key
     */
    protected void purgeKeys() {
        if (!this.purge.compareAndSet(false, true)) {
            return;
        }
        try {
            Reference<? extends K> reference = null;
            ReferenceQueue<K> referenceQueue = this.referenceQueue;
            while ((reference = referenceQueue.poll()) != null) {
                this.target.remove(reference);
            }
        } finally {
            this.purge.set(false);
        }
    }

    /**
     * 引用类型
     */
    public enum ReferenceType {
        SOFT,
        WEAK,
        ;
    }

    private static class SoftKey<T> extends SoftReference<T> {
        private final int hash;

        public SoftKey(T reference, ReferenceQueue<T> referenceQueue) {
            super(reference, referenceQueue);
            this.hash = reference.hashCode();
        }

        @Override
        public int hashCode() {
            return this.hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof SoftKey) {
                return Objects.equals(this.get(), ((SoftKey<?>) obj).get());
            }
            return false;
        }

        @Override
        public String toString() {
            T reference = this.get();
            return reference == null ? "soft key has been cleared, hash: " + this.hashCode() : reference.toString();
        }
    }

    private static class WeakKey<T> extends WeakReference<T> {
        private final int hash;

        public WeakKey(T reference, ReferenceQueue<T> referenceQueue) {
            super(reference, referenceQueue);
            this.hash = reference.hashCode();
        }

        @Override
        public int hashCode() {
            return this.hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof WeakKey) {
                return Objects.equals(this.get(), ((WeakKey<?>) obj).get());
            }
            return false;
        }

        @Override
        public String toString() {
            T reference = this.get();
            return reference == null ? "weak key has been cleared, hash: " + this.hashCode() : reference.toString();
        }
    }
}
