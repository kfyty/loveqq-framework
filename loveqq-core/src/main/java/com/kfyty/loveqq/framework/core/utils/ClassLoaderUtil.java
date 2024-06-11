package com.kfyty.loveqq.framework.core.utils;

/**
 * 描述: 类加载器工具
 *
 * @author kfyty725
 * @date 2023/3/15 19:51
 * @email kfyty725@hotmail.com
 */
public abstract class ClassLoaderUtil {
    /**
     * 返回类加载器
     *
     * @param clazz 调用 class
     * @return 类加载器
     */
    public static ClassLoader classLoader(Class<?> clazz) {
        return Thread.currentThread().getContextClassLoader();
    }
}
