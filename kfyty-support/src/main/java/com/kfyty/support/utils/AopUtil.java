package com.kfyty.support.utils;

import com.kfyty.support.exception.SupportException;
import com.kfyty.support.proxy.MethodInterceptorChainPoint;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodInvocationInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static com.kfyty.support.utils.ReflectUtil.getFieldMap;
import static com.kfyty.support.utils.ReflectUtil.getFieldValue;
import static com.kfyty.support.utils.ReflectUtil.getMethod;
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
    private static final String CGLIB_CLASS_SEPARATOR = "$$EnhancerByCGLIB$$";

    private static final String CGLIB_PROXY_CALLBACK_FIELD = "CGLIB$CALLBACK_";

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
        return Proxy.isProxyClass(instance.getClass());
    }

    /**
     * 测试给定实例是否是 cglib 代理
     *
     * @param instance 实例
     * @return true if cglib proxy
     */
    public static boolean isCglibProxy(Object instance) {
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
        if (Proxy.isProxyClass(targetClass) || method.getDeclaringClass().equals(targetClass)) {
            return method;
        }
        return ofNullable(getMethod(targetClass, method.getName(), method.getParameterTypes())).orElse(method);
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
     * @param bean                  代理 bean
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
            throw new SupportException("the instance is not a proxy !");
        }
        Object interceptorChain = isJdkProxy(proxy) ? Proxy.getInvocationHandler(proxy) :
                getFieldMap(proxy.getClass()).entrySet().stream().filter(e -> e.getKey().startsWith(CGLIB_PROXY_CALLBACK_FIELD)).map(e -> getFieldValue(proxy, e.getValue())).filter(e -> e instanceof MethodInterceptorChain).findAny().orElse(null);
        if (interceptorChain instanceof MethodInterceptorChain) {
            return (MethodInterceptorChain) interceptorChain;
        }
        throw new SupportException("the proxy object has no MethodInterceptorChain: " + proxy);
    }
}
