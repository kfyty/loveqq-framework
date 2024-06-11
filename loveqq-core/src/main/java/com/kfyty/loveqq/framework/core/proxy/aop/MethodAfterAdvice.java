package com.kfyty.loveqq.framework.core.proxy.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * 描述: 方法后置通知
 *
 * @author kfyty725
 * @date 2021/7/29 16:02
 * @email kfyty725@hotmail.com
 */
public interface MethodAfterAdvice extends MethodInterceptor {

    @Override
    default Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } finally {
            this.after(invocation.getMethod(), invocation.getArguments(), invocation.getThis());
        }
    }

    void after(Method method, Object[] args, Object target) throws Throwable;
}
