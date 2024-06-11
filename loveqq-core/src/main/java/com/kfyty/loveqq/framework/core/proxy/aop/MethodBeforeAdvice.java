package com.kfyty.loveqq.framework.core.proxy.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * 描述: 方法前置通知
 *
 * @author kfyty725
 * @date 2021/7/29 16:02
 * @email kfyty725@hotmail.com
 */
public interface MethodBeforeAdvice extends MethodInterceptor {

    @Override
    default Object invoke(MethodInvocation invocation) throws Throwable {
        this.before(invocation.getMethod(), invocation.getArguments(), invocation.getThis());
        return invocation.proceed();
    }

    void before(Method method, Object[] args, Object target) throws Throwable;
}
