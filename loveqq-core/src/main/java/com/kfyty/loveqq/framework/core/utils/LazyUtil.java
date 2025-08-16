package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Lazy;

import java.lang.reflect.Method;

/**
 * 描述: 懒加载工具
 *
 * @author kfyty725
 * @date 2021/8/6 20:40
 * @email kfyty725@hotmail.com
 */
public abstract class LazyUtil {
    /**
     * 解析 {@link Lazy} 注解
     *
     * @param clazz class
     * @return {@link Lazy}
     */
    public static Lazy resolveLazy(Class<?> clazz) {
        return AnnotationUtil.findAnnotation(clazz, Lazy.class);
    }

    /**
     * 解析 {@link Lazy} 注解
     * 方法上不存在时，要解析方法声明类的注解，因为类懒加载，方法必懒加载
     *
     * @param beanMethod method
     * @return {@link Lazy}
     */
    public static Lazy resolveLazy(Method beanMethod) {
        Lazy lazy = AnnotationUtil.findAnnotation(beanMethod, Lazy.class);
        if (lazy == null) {
            lazy = AnnotationUtil.findAnnotation(beanMethod.getDeclaringClass(), Lazy.class);
        }
        return lazy;
    }
}
