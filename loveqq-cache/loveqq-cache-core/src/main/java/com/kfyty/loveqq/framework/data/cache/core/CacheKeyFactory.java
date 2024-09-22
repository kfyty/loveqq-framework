package com.kfyty.loveqq.framework.data.cache.core;

import java.lang.reflect.Method;

/**
 * 描述: 缓存 key 工厂
 *
 * @author kfyty725
 * @date 2024/9/22 10:51
 * @email kfyty725@hotmail.com
 */
public interface CacheKeyFactory {
    /**
     * 构建缓存 key
     *
     * @param method 目标方法
     * @param args   方法参数
     * @param target 方法所在实例
     * @return 缓存 key
     */
    String buildKey(Method method, Object[] args, Object target);
}
