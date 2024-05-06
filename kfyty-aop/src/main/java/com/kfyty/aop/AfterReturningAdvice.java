package com.kfyty.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * 描述: 方法后置通知
 *
 * @author kfyty725
 * @date 2021/7/29 16:03
 * @email kfyty725@hotmail.com
 */
public interface AfterReturningAdvice extends MethodInterceptor {

    @Override
    default Object invoke(MethodInvocation invocation) throws Throwable {
        Object retValue = invocation.proceed();
        this.afterReturning(retValue, invocation.getMethod(), invocation.getArguments(), invocation.getThis());
        return retValue;
    }

    void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable;
}
