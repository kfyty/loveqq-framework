package com.kfyty.support.proxy;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 描述: 方法调用拦截，统一 JDK 与 Cglib
 *
 * @author kfyty725
 * @date 2021/6/19 11:06
 * @email kfyty725@hotmail.com
 */
public abstract class MethodInvocationInterceptor implements InvocationHandler, MethodInterceptor {
    protected final Object target;

    public MethodInvocationInterceptor(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return this.process(new MethodProxyWrapper(this.target, proxy, method, args));
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        return this.process(new MethodProxyWrapper(this.target, proxy, method, args, methodProxy));
    }

    public Object getTarget() {
        return this.target;
    }

    protected abstract Object process(MethodProxyWrapper methodProxy) throws Throwable;
}
