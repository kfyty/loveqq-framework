package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.MethodInvocationInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

/**
 * 描述: aop 动态代理工具
 *
 * @author kfyty725
 * @date 2021/6/19 11:34
 * @email kfyty725@hotmail.com
 */
public abstract class AopUtil {
    public static final String CGLIB_TAG = "ByLoveqqFramework";

    public static final String CGLIB_CLASS_SEPARATOR = "$$Enhancer" + CGLIB_TAG + "$$";

    public static final String CGLIB_PROXY_CALLBACK_FIELD = "CGLIB$CALLBACK_0";

    /**
     * 测试给定实例是否是代理对象
     *
     * @param instance 实例
     * @return true if proxy object
     */
    public static boolean isProxy(Object instance) {
        return isJdkProxy(instance) || isCglibProxy(instance);
    }

    /**
     * 测试给定实例是否是 jdk 代理
     *
     * @param instance 实例
     * @return true if jdk proxy
     */
    public static boolean isJdkProxy(Object instance) {
        if (instance instanceof Class<?>) {
            return Proxy.isProxyClass((Class<?>) instance);
        }
        return Proxy.isProxyClass(instance.getClass());
    }

    /**
     * 测试给定实例是否是 cglib 代理
     *
     * @param instance 实例
     * @return true if cglib proxy
     */
    public static boolean isCglibProxy(Object instance) {
        if (instance instanceof Class<?>) {
            return ((Class<?>) instance).getName().contains(CGLIB_CLASS_SEPARATOR);
        }
        return instance.getClass().getName().contains(CGLIB_CLASS_SEPARATOR);
    }

    /**
     * 获取代理的目标 bean，若不存在，则返回当前 bean
     *
     * @param bean bean
     * @return 原 bean
     */
    public static Object getTarget(Object bean) {
        if (!isProxy(bean)) {
            return bean;
        }
        if (isCglibProxy(bean)) {
            return of(getProxyInterceptorChain(bean)).map(MethodInvocationInterceptor::getTarget).orElse(bean);
        }
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(bean);
        if (!(invocationHandler instanceof MethodInterceptorChain)) {
            return invocationHandler;
        }
        return of(invocationHandler).map(e -> ((MethodInterceptorChain) e).getTarget()).orElse(bean);
    }

    /**
     * 获取代理的目标 bean 的类型，若不存在，则返回当前 bean 的类型
     *
     * @param bean bean
     * @return 原 bean 的类型
     */
    public static Class<?> getTargetClass(Object bean) {
        return getTarget(bean).getClass();
    }

    /**
     * 获取代理目标中声明的方法, 如果代理目标是 jdk 代理, 则直接返回
     *
     * @param targetClass 原目标类型
     * @param method      可能是代理方法或接口中的方法
     * @return 获取原目标中声明的方法
     */
    public static Method getTargetMethod(Class<?> targetClass, Method method) {
        if (isJdkProxy(targetClass) || method.getDeclaringClass().equals(targetClass) || method.getDeclaringClass() == Object.class) {
            return method;
        }
        return ofNullable(ReflectUtil.getMethod(targetClass, method.getName(), method.getParameterTypes())).orElse(method);
    }

    /**
     * 获取接口中声明的方法
     *
     * @param bean   bean 可能是代理
     * @param method 方法
     * @return 接口中声明的方法
     */
    public static Method getInterfaceMethod(Object bean, Method method) {
        if (!isJdkProxy(bean)) {
            return method;
        }
        while (!method.getDeclaringClass().isInterface()) {
            Method superMethod = ReflectUtil.getSuperMethod(method);
            if (superMethod == null) {
                break;
            }
            method = superMethod;
        }
        return method;
    }

    /**
     * 向给定的 bean 中添加代理拦截点
     *
     * @param bean                        代理 bean
     * @param methodInterceptorChainPoint 拦截点
     * @return true if success
     */
    public static boolean addProxyInterceptorPoint(Object bean, MethodInterceptorChainPoint methodInterceptorChainPoint) {
        if (isCglibProxy(bean)) {
            getProxyInterceptorChain(bean).addInterceptorPoint(methodInterceptorChainPoint);
            return true;
        }
        if (!isJdkProxy(bean)) {
            return false;
        }
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(bean);
        if (!(invocationHandler instanceof MethodInterceptorChain)) {
            return false;
        }
        ((MethodInterceptorChain) invocationHandler).addInterceptorPoint(methodInterceptorChainPoint);
        return true;
    }

    /**
     * 获取代理中的拦截链
     *
     * @param proxy 代理对象
     * @return 拦截链
     */
    public static MethodInterceptorChain getProxyInterceptorChain(Object proxy) {
        if (!isProxy(proxy)) {
            throw new ResolvableException("The instance is not a proxy: " + proxy);
        }
        Object interceptorChain = isJdkProxy(proxy) ? Proxy.getInvocationHandler(proxy) : ReflectUtil.getFieldValue(proxy, CGLIB_PROXY_CALLBACK_FIELD);
        if (interceptorChain instanceof MethodInterceptorChain) {
            return (MethodInterceptorChain) interceptorChain;
        }
        throw new ResolvableException("The proxy object has no MethodInterceptorChain: " + proxy);
    }
}
