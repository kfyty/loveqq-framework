package com.kfyty.loveqq.framework.data.cache.core;

/**
 * 描述: 抽象缓存实现
 *
 * @author kfyty725
 * @date 2024/7/4 10:18
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractCache implements Cache {

    @Override
    public <T> T get(String name) {
        return this.getInternal(name);
    }

    @Override
    public <T> T get(String name, Class<T> clazz) {
        T cache = this.getInternal(name);
        if (cache != null && cache != NullValue.INSTANCE && clazz != null && !clazz.isInstance(cache)) {
            throw new IllegalArgumentException("Cached value is not of required type [" + clazz + "]: " + cache);
        }
        return cache;
    }

    protected abstract <T> T getInternal(String name);
}
