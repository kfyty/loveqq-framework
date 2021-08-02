package com.kfyty.support.wrapper;

import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * 描述: 可用于弱引用缓存的 key
 *
 * @author kfyty725
 * @date 2021/8/3 11:08
 * @email kfyty725@hotmail.com
 */
public class WeakKey<T> extends WeakReference<T> {
    private final int hash;

    public WeakKey(T reference) {
        super(reference);
        this.hash = reference.hashCode();
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WeakKey)) {
            return false;
        }
        Object self = get();
        Object other = ((WeakKey<?>) obj).get();
        return Objects.equals(self, other);
    }

    @Override
    public String toString() {
        T reference = this.get();
        return reference == null ? "weak key has been cleared, hash: " + this.hashCode() : reference.toString();
    }
}
