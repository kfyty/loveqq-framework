package com.kfyty.aop;

import com.kfyty.support.proxy.MethodInterceptorChainPoint;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxy;
import org.aopalliance.aop.Advice;

import java.lang.reflect.Method;

/**
 * 描述: 异常通知
 *
 * @author kfyty725
 * @date 2021/7/29 16:04
 * @email kfyty725@hotmail.com
 */
public interface ThrowsAdvice extends Advice, MethodInterceptorChainPoint {

    @Override
    default Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        try {
            return chain.proceed(methodProxy);
        } catch (Throwable throwable) {
            this.afterThrowing(methodProxy.getTargetMethod(), methodProxy.getArguments(), methodProxy.getTarget(), throwable);
            throw throwable;
        }
    }

    void afterThrowing(Method method, Object[] args, Object target, Throwable throwable);
}
