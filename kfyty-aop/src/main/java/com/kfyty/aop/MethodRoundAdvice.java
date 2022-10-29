package com.kfyty.aop;

import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.core.proxy.MethodInterceptorChain;
import com.kfyty.core.proxy.MethodProxy;
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
