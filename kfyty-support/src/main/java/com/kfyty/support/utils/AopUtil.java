package com.kfyty.support.utils;

import com.kfyty.support.exception.SupportException;
import com.kfyty.support.proxy.InterceptorChain;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * 描述: aop 动态代理工具
 *
 * @author kfyty725
 * @date 2021/6/19 11:34
 * @email kfyty725@hotmail.com
 */
public abstract class AopUtil {
    private static final String CGLIB_CLASS_SEPARATOR = "$$";
    private static final String CGLIB_PROXY_CALLBACK_FIELD = "CGLIB$CALLBACK_";

    public static boolean isProxy(Object instance) {
        return isJdkProxy(instance) || isCglibProxy(instance);
    }

    public static boolean isJdkProxy(Object instance) {
        return Proxy.isProxyClass(instance.getClass());
    }

    public static boolean isCglibProxy(Object instance) {
        return instance.getClass().getName().contains(CGLIB_CLASS_SEPARATOR);
    }

    public static Object getSourceIfNecessary(Object bean) {
        return isJdkProxy(bean) ? getInterceptorChain(bean).getSource() : bean;
    }

    public static InterceptorChain getInterceptorChain(Object proxy) {
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
                    if(fieldValue instanceof InterceptorChain) {
                        interceptorChain = fieldValue;
                        break;
                    }
                }
            }
        }
        if(interceptorChain instanceof InterceptorChain) {
            return (InterceptorChain) interceptorChain;
        }
        throw new SupportException("the proxy object has no InterceptorChain !");
    }
}
