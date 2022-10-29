package com.kfyty.aop;

import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.core.proxy.MethodInterceptorChain;
import com.kfyty.core.proxy.MethodProxy;
import org.aopalliance.aop.Advice;

import java.lang.reflect.Method;

/**
 * 描述: 方法前置通知
 *
 * @author kfyty725
 * @date 2021/7/29 16:02
 * @email kfyty725@hotmail.com
 */
public interface MethodBeforeAdvice extends Advice, MethodInterceptorChainPoint {

    @Override
    default Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        this.before(methodProxy.getTargetMethod(), methodProxy.getArguments(), methodProxy.getTarget());
        return chain.proceed(methodProxy);
    }

    void before(Method method, Object[] args, Object target) throws Throwable;
}
