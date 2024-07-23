package com.kfyty.loveqq.framework.data.cache.core.reactive;

import com.kfyty.loveqq.framework.data.cache.core.NullValue;
import reactor.core.publisher.Mono;

/**
 * 描述: 响应式抽象缓存实现
 *
 * @author kfyty725
 * @date 2024/7/4 10:18
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractReactiveCache implements ReactiveCache {

    @Override
    public <T> Mono<T> getAsync(String name) {
        return this.getInternalAsync(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> getAsync(String name, Class<T> clazz) {
        return this.getInternalAsync(name)
                .filter(cache -> {
                    if (cache != null && cache != NullValue.INSTANCE && clazz != null && !clazz.isInstance(cache)) {
                        throw new IllegalArgumentException("Cached value is not of required type [" + clazz + "]: " + cache);
                    }
                    return true;
                })
                .map(e -> (T) e);
    }

    protected abstract <T> Mono<T> getInternalAsync(String name);
}
