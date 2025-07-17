package com.kfyty.loveqq.framework.core.lang.util.concurrent;

import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.thread.SingleThreadTask;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;
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
        this.referenceType = referenceType;
        this.referenceQueue = new ReferenceQueue<>();
        ReferenceManager.INSTANCE.registry(this.referenceQueue, this.target);
        ReferenceManager.INSTANCE.start();
    }

    @Override
    public int size() {
        return this.target.size();
    }

    @Override
    public boolean isEmpty() {
        return this.target.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(Object key) {
        return this.target.containsKey(this.wrapKey((K) key, null));
    }

    @Override
    public boolean containsValue(Object value) {
        return this.target.containsValue(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        return this.target.get(this.wrapKey((K) key, null));
    }

    @Override
    public V put(K key, V value) {
        return this.target.put(this.wrapKey(key, this.referenceQueue), value);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return this.target.putIfAbsent(this.wrapKey(key, this.referenceQueue), value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public V replace(K key, V value) {
        return this.target.replace(this.wrapKey(key, this.referenceQueue), value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return this.target.replace(this.wrapKey(key, this.referenceQueue), oldValue, newValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        return this.target.remove(this.wrapKey((K) key, null));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object key, Object value) {
        return this.target.remove(this.wrapKey((K) key, null), value);
    }

    @Override
    public void clear() {
        this.target.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.target.keySet().stream().map(Reference::get).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public Collection<V> values() {
        return this.target.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.target.entrySet()
                .stream()
                .map(entry -> new AbstractMap.SimpleImmutableEntry<>(entry.getKey().get(), entry.getValue()))
                .filter(e -> e.getKey() != null)
                .collect(Collectors.toSet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getOrDefault(Object key, V defaultValue) {
        return this.target.getOrDefault(this.wrapKey((K) key, null), defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        this.target.forEach((k, v) -> action.accept(k.get(), v));
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        this.target.replaceAll((k, v) -> function.apply(k.get(), v));
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return this.target.computeIfAbsent(this.wrapKey(key, this.referenceQueue), referenceKey -> mappingFunction.apply(key));
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return this.target.computeIfPresent(this.wrapKey(key, this.referenceQueue), (referenceKey, value) -> remappingFunction.apply(key, value));
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return this.target.compute(this.wrapKey(key, this.referenceQueue), (referenceKey, value) -> remappingFunction.apply(key, value));
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
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
            if (obj instanceof SoftKey<?> other) {
                return Objects.equals(this.get(), other.get());
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
            if (obj instanceof WeakKey<?> other) {
                return Objects.equals(this.get(), other.get());
            }
            return false;
        }

        @Override
        public String toString() {
            T reference = this.get();
            return reference == null ? "weak key has been cleared, hash: " + this.hashCode() : reference.toString();
        }
    }

    private static class ReferenceManager extends SingleThreadTask {
        /**
         * 单例
         */
        private static final ReferenceManager INSTANCE = new ReferenceManager();

        /**
         * 监听队列
         */
        private final Queue<Pair<ReferenceQueue<?>, Map<?, ?>>> references;

        private ReferenceManager() {
            super("reference-manager-thread");
            this.references = new LinkedBlockingDeque<>();
        }

        public void registry(ReferenceQueue<?> referenceQueue, Map<?, ?> target) {
            this.references.add(new Pair<>(referenceQueue, target));
        }

        @Override
        protected void sleep() {
            CommonUtil.sleep(3000);
        }

        @Override
        public void doRun() {
            Reference<?> reference = null;
            for (Pair<ReferenceQueue<?>, Map<?, ?>> referencePair : this.references) {
                ReferenceQueue<?> referenceQueue = referencePair.getKey();
                Map<?, ?> target = referencePair.getValue();
                while ((reference = referenceQueue.poll()) != null) {
                    target.remove(reference);
                }
            }
        }
    }
}
