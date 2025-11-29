package com.kfyty.loveqq.framework.data.cache.core;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/9/22 10:55
 * @email kfyty725@hotmail.com
 */
public class DefaultCacheKeyFactory implements CacheKeyFactory {

    @Override
    public String buildKey(Method method, Object[] args, Object target) {
        StringBuilder key = new StringBuilder(target.getClass().getName()).append('#').append(method.getName()).append(':');

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            key.append(parameters[i].getName()).append('=').append(args[i]);
            if (i != parameters.length - 1) {
                key.append(':');
            }
        }

        return key.toString();
    }
}
