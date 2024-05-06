package com.kfyty.aop.support;

import com.kfyty.core.proxy.MethodInterceptorChain;
import com.kfyty.core.proxy.MethodProxy;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * 描述: {@link MethodProxy} 与 {@link MethodInvocation} 适配器
 *
 * @author kfyty725
 * @date 2022/3/20 16:47
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class MethodProxyInvocationAdapter implements MethodInvocation {
    private final MethodProxy methodProxy;
    private final MethodInterceptorChain chain;

    @Override
    public Object[] getArguments() {
        return this.methodProxy.getArguments();
    }

    @Override
    public Object proceed() throws Throwable {
        return chain.proceed(methodProxy);
    }

    @Override
    public Object getThis() {
        return this.methodProxy.getTarget();
    }

    @Override
    public AccessibleObject getStaticPart() {
        return this.methodProxy.getTargetMethod();
    }

    @Override
    public Method getMethod() {
        return this.methodProxy.getTargetMethod();
    }
}
