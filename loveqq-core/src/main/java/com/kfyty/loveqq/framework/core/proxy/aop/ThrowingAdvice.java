package com.kfyty.loveqq.framework.core.proxy.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * 描述: 异常通知
 *
 * @author kfyty725
 * @date 2021/7/29 16:04
 * @email kfyty725@hotmail.com
 */
public interface ThrowingAdvice extends MethodInterceptor {

    @Override
    default Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (Throwable throwable) {
            this.afterThrowing(invocation.getMethod(), invocation.getArguments(), invocation.getThis(), throwable);
            throw throwable;
        }
    }

    void afterThrowing(Method method, Object[] args, Object target, Throwable throwable);
}
