package com.kfyty.support.utils;

import com.kfyty.support.exception.SupportException;
import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.proxy.MethodInterceptorChain;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;

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
     * @param instance 实例
     * @return true if proxy object
     */
    public static boolean isProxy(Object instance) {
        return isJdkProxy(instance) || isCglibProxy(instance);
    }

    /**
     * 测试给定实例是否是 jdk 代理
     * @param instance 实例
     * @return true if jdk proxy
     */
    public static boolean isJdkProxy(Object instance) {
        return Proxy.isProxyClass(instance.getClass());
    }

    /**
     * 测试给定实例是否是 cglib 代理
     * @param instance 实例
     * @return true if cglib proxy
     */
    public static boolean isCglibProxy(Object instance) {
        return instance.getClass().getName().contains(CGLIB_CLASS_SEPARATOR);
    }

    /**
     * 获取原 bean，若原 bean 不存在，则返回当前 bean
     * @param bean bean
     * @return 原 bean
     */
    public static Object getSourceTarget(Object bean) {
        if(!isProxy(bean)) {
            return bean;
        }
        if (isJdkProxy(bean)) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(bean);
            if (!(invocationHandler instanceof MethodInterceptorChain)) {
                return invocationHandler;
            }
            return Optional.of(invocationHandler).map(e -> ((MethodInterceptorChain) e).getSource()).orElse(bean);
        }
        MethodInterceptorChain interceptorChain = getProxyInterceptorChain(bean);
        return interceptorChain.getSource() == null ? bean : interceptorChain.getSource();
    }

    /**
     * 获取原 bean 的类型，若原 bean 不存在，则返回当前 bean 的类型
     * @param bean bean
     * @return 原 bean 的类型
     */
    public static Class<?> getSourceClass(Object bean) {
        return getSourceTarget(bean).getClass();
    }

    /**
     * 获取原目标中声明的方法
     * @param sourceClass 原目标类型
     * @param method 可能是代理方法或接口中的方法
     * @return 获取原目标中声明的方法
     */
    public static Method getSourceTargetMethod(Class<?> sourceClass, Method method) {
        if (!method.getDeclaringClass().equals(sourceClass)) {
            Method specificMethod = ReflectUtil.getMethod(sourceClass, method.getName(), method.getParameterTypes());
            return specificMethod == null ? method : specificMethod;
        }
        return method;
    }

    /**
     * 向给定的 bean 中添加代理拦截点
     * @param bean 代理 bean
     * @param interceptorChainPoint 拦截点
     * @return true if success
     */
    public static boolean addProxyInterceptorPoint(Object bean, InterceptorChainPoint interceptorChainPoint) {
        if (!isProxy(bean)) {
            return false;
        }
        getProxyInterceptorChain(bean).addInterceptorPoint(interceptorChainPoint);
        return true;
    }

    /**
     * 获取代理中的拦截链
     * @param proxy 代理对象
     * @return 拦截链
     */
    public static MethodInterceptorChain getProxyInterceptorChain(Object proxy) {
        if(!isProxy(proxy)) {
            throw new SupportException("the instance is not a proxy !");
        }
        Object interceptorChain = null;
        if(isJdkProxy(proxy)) {
            interceptorChain = Proxy.getInvocationHandler(proxy);
        } else {
            for (Map.Entry<String, Field> entry : ReflectUtil.getFieldMap(proxy.getClass()).entrySet()) {
                if(entry.getKey().startsWith(CGLIB_PROXY_CALLBACK_FIELD)) {
                    Object fieldValue = ReflectUtil.getFieldValue(proxy, entry.getValue());
                    if(fieldValue instanceof MethodInterceptorChain) {
                        interceptorChain = fieldValue;
                        break;
                    }
                }
            }
        }
        if(interceptorChain instanceof MethodInterceptorChain) {
            return (MethodInterceptorChain) interceptorChain;
        }
        throw new SupportException("the proxy object has no MethodInterceptorChain: " + proxy);
    }
}
