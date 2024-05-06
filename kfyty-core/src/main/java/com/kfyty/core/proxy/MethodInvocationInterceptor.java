package com.kfyty.core.proxy;

import lombok.Getter;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 描述: 方法调用拦截，统一 JDK 与 Cglib
 *
 * @author kfyty725
 * @date 2021/6/19 11:06
 * @email kfyty725@hotmail.com
 */
@Getter
public abstract class MethodInvocationInterceptor implements InvocationHandler, MethodInterceptor {
    protected final Object target;

    public MethodInvocationInterceptor(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return this.invoke(new MethodProxy(this.target, proxy, method, args));
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, net.sf.cglib.proxy.MethodProxy methodProxy) throws Throwable {
        return this.invoke(new MethodProxy(this.target, proxy, method, args, methodProxy));
    }

    protected abstract Object invoke(MethodProxy methodProxy) throws Throwable;
}
