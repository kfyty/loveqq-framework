package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.MethodInvocationInterceptor;
import javassist.util.proxy.ProxyFactory;

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
    /**
     * 代理类名称标签
     */
    public static final String PROXY_TAG = "EnhancerByLoveqqFramework";

    /**
     * 测试给定实例是否是代理对象
     *
     * @param instance 实例
     * @return true if proxy object
     */
    public static boolean isProxy(Object instance) {
        return isJdkProxy(instance) || isClassProxy(instance);
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
    public static boolean isClassProxy(Object instance) {
        if (instance instanceof Class<?>) {
            return ProxyFactory.isProxyClass((Class<?>) instance);
        }
        return ProxyFactory.isProxyClass(instance.getClass());
    }

    /**
     * 获取代理的目标 bean，若不存在，则返回当前 bean
     *
     * @param bean bean
     * @return 原 bean
     */
    public static Object getTarget(Object bean) {
        if (isClassProxy(bean)) {
            return of(getProxyInterceptorChain(bean)).map(MethodInvocationInterceptor::getTarget).orElse(bean);
        }
        if (isJdkProxy(bean)) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(bean);
            if (invocationHandler instanceof MethodInterceptorChain chain) {
                return ofNullable(chain.getTarget()).orElse(bean);
            }
            return invocationHandler;
        }
        return bean;
    }

    /**
     * 获取代理的目标 bean 的类型，若不存在，则返回当前 bean 的类型
     *
     * @param bean bean
     * @return 原 bean 的类型
     */
    public static Class<?> getTargetClass(Object bean) {
        Object target = getTarget(bean);
        if (bean == target && isClassProxy(bean)) {
            return bean.getClass().getSuperclass();
        }
        return target.getClass();
    }

    /**
     * 获取代理目标中声明的方法, 如果代理目标是 jdk 代理, 则直接返回
     *
     * @param targetClass 原目标类型
     * @param method      可能是代理方法或接口中的方法
     * @return 获取原目标中声明的方法
     */
    public static Method getTargetMethod(Class<?> targetClass, Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        if (isJdkProxy(targetClass) || declaringClass == targetClass || declaringClass == Object.class) {
            return method;
        }
        return ofNullable(ReflectUtil.getMethod(targetClass, method.getName(), method.getParameterTypes())).orElse(method);
    }

    /**
     * 获取接口中声明的方法
     *
     * @param beanClass   bean class
     * @param method 方法
     * @return 接口中声明的方法
     */
    public static Method getInterfaceMethod(Class<?> beanClass, Method method) {
        if (beanClass.getInterfaces().length < 1) {
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
        if (isClassProxy(bean)) {
            getProxyInterceptorChain(bean).addInterceptorPoint(methodInterceptorChainPoint);
            return true;
        }
        if (isJdkProxy(bean)) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(bean);
            if (invocationHandler instanceof MethodInterceptorChain) {
                ((MethodInterceptorChain) invocationHandler).addInterceptorPoint(methodInterceptorChainPoint);
                return true;
            }
        }
        return false;
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
        Object interceptorChain = isJdkProxy(proxy) ? Proxy.getInvocationHandler(proxy) : ProxyFactory.getHandler((javassist.util.proxy.Proxy) proxy);
        if (interceptorChain instanceof MethodInterceptorChain chain) {
            return chain;
        }
        throw new ResolvableException("The proxy object has no MethodInterceptorChain: " + proxy);
    }
}
