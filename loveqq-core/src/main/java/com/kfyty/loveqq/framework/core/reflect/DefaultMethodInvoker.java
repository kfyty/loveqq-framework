package com.kfyty.loveqq.framework.core.reflect;

import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 描述: 接口默认方法执行器
 *
 * @author kfyty725
 * @date 2024/8/3 20:04
 * @email kfyty725@hotmail.com
 */
public class DefaultMethodInvoker {
    private static final int ALLOWED_MODES = MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC;

    private static Method privateLookupInMethod;

    private static Constructor<MethodHandles.Lookup> lookupConstructor;

    static {
        try {
            privateLookupInMethod = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
        } catch (NoSuchMethodException e) {
            // ignored
        }

        if (privateLookupInMethod == null) {
            try {
                lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
                lookupConstructor.setAccessible(true);
            } catch (NoSuchMethodException e) {
                // ignored
            }
        }
    }

    public static Object invokeDefault(Object target, Method method) {
        try {
            if (privateLookupInMethod != null) {
                return getMethodHandleByLookupInMethod(method).bindTo(target).invokeWithArguments();
            }
            return getMethodHandleByLookupConstructor(method).bindTo(target).invokeWithArguments();
        } catch (Throwable e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static MethodHandle getMethodHandleByLookupInMethod(Method method) throws Exception {
        Class<?> declaringClass = method.getDeclaringClass();
        return ((MethodHandles.Lookup) privateLookupInMethod.invoke(null, declaringClass, MethodHandles.lookup()))
                .findSpecial(declaringClass, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()), declaringClass);
    }

    public static MethodHandle getMethodHandleByLookupConstructor(Method method) throws Exception {
        Class<?> declaringClass = method.getDeclaringClass();
        return lookupConstructor.newInstance(declaringClass, ALLOWED_MODES).unreflectSpecial(method, declaringClass);
    }
}
