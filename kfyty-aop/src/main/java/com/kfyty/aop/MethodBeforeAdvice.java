package com.kfyty.aop;

import org.aopalliance.aop.Advice;

import java.lang.reflect.Method;

/**
 * 描述: 方法前置通知
 *
 * @author kfyty725
 * @date 2021/7/29 16:02
 * @email kfyty725@hotmail.com
 */
public interface MethodBeforeAdvice extends Advice {
    void before(Method method, Object[] args, Object target) throws Throwable;
}
