package com.kfyty.loveqq.framework.core.proxy;

import javassist.util.proxy.MethodHandler;
import lombok.Getter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 描述: 方法调用拦截，统一 JDK 与 Javassist
 *
 * @author kfyty725
 * @date 2021/6/19 11:06
 * @email kfyty725@hotmail.com
 */
@Getter
public abstract class MethodInvocationInterceptor implements InvocationHandler, MethodHandler {
    protected final Object target;

    public MethodInvocationInterceptor(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return this.invoke(new MethodProxy(this.target, proxy, method, args));
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        return this.invoke(new MethodProxy(this.target, self, thisMethod, args, proceed));
    }

    protected abstract Object invoke(MethodProxy methodProxy) throws Throwable;
}
