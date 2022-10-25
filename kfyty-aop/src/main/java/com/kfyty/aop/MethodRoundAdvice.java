package com.kfyty.aop;

import com.kfyty.support.proxy.MethodInterceptorChainPoint;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxy;
import org.aopalliance.aop.Advice;
import org.aspectj.lang.ProceedingJoinPoint;

import static com.kfyty.aop.proxy.ExposeInvocationInterceptorProxy.currentJoinPoint;

/**
 * 描述: 方法环绕通知
 *
 * @author kfyty725
 * @date 2021/7/29 16:04
 * @email kfyty725@hotmail.com
 */
public interface MethodRoundAdvice extends Advice, MethodInterceptorChainPoint {

    @Override
    default Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        return this.around((ProceedingJoinPoint) currentJoinPoint());
    }

    Object around(ProceedingJoinPoint pjp) throws Throwable;
}
