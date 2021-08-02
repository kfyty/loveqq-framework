package com.kfyty.aop;

import org.aopalliance.aop.Advice;

import java.lang.reflect.Method;

/**
 * 描述: 方法后置通知
 *
 * @author kfyty725
 * @date 2021/7/29 16:03
 * @email kfyty725@hotmail.com
 */
public interface AfterReturningAdvice extends Advice {
    void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable;
}
