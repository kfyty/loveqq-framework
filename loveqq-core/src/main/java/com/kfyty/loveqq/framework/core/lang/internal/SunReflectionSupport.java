package com.kfyty.loveqq.framework.core.lang.internal;

import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.load;

/**
 * 描述: sun 反射支持
 *
 * @author kfyty725
 * @date 2021/6/19 11:50
 * @email kfyty725@hotmail.com
 */
public class SunReflectionSupport {
    /**
     * @see Unsafe
     */
    private static Unsafe unsafe;

    /**
     * @see sun.reflect.ReflectionFactory
     */
    private static ReflectionFactory reflectionFactory;

    static {
        try {
            unsafe = getUnSafe();
        } catch (Throwable e) {
            // ignored
        }

        try {
            reflectionFactory = createReflectionFactory();
        } catch (Throwable e) {
            // ignored
        }
    }

    public static boolean isSupport() {
        return unsafe != null || reflectionFactory != null;
    }

    public static <T> T newInstance(Class<T> clazz) {
        if (unsafe != null) {
            return allocateInstance(clazz);
        }
        return ReflectUtil.newInstance(createConstructor(clazz));
    }

    public static Unsafe getUnSafe() {
        if (unsafe != null) {
            return unsafe;
        }
        Field field = ReflectUtil.getField(Unsafe.class, "theUnsafe");
        return (Unsafe) ReflectUtil.getFieldValue(null, field);
    }

    @SuppressWarnings("unchecked")
    public static <T> T allocateInstance(Class<T> clazz) {
        try {
            return (T) unsafe.allocateInstance(clazz);
        } catch (Throwable e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> createConstructor(Class<T> clazz) {
        return (Constructor<T>) reflectionFactory.newConstructorForSerialization(clazz, ReflectUtil.getConstructor(Object.class));
    }

    public static ReflectionFactory createReflectionFactory() {
        if (reflectionFactory != null) {
            return reflectionFactory;
        }
        Class<?> reflectionFactoryClass = load(System.getProperty("sun.reflect.ReflectionFactory", "sun.reflect.ReflectionFactory"));
        return createReflectionFactory(reflectionFactoryClass);
    }

    public static ReflectionFactory createReflectionFactory(Class<?> reflectionFactoryClass) {
        if (reflectionFactory != null && reflectionFactory.getClass() == reflectionFactoryClass) {
            return reflectionFactory;
        }
        Method method = ReflectUtil.getMethod(reflectionFactoryClass, "getReflectionFactory");
        return (ReflectionFactory) ReflectUtil.invokeMethod(null, method);
    }
}
