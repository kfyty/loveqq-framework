package com.kfyty.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;

import static com.kfyty.aop.proxy.ExposeInvocationInterceptorProxy.currentJoinPoint;

/**
 * 描述: 方法环绕通知
 *
 * @author kfyty725
 * @date 2021/7/29 16:04
 * @email kfyty725@hotmail.com
 */
public interface MethodAroundAdvice extends MethodInterceptor {

    @Override
    default Object invoke(MethodInvocation invocation) throws Throwable {
        return this.around((ProceedingJoinPoint) currentJoinPoint());
    }

    Object around(ProceedingJoinPoint pjp) throws Throwable;
}
