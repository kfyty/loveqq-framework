package com.kfyty.aop;

import org.aopalliance.aop.Advice;

import java.lang.reflect.Method;

/**
 * 描述: 方法后置通知
 *
 * @author kfyty725
 * @date 2021/7/29 16:02
 * @email kfyty725@hotmail.com
 */
public interface MethodAfterAdvice extends Advice {
    void after(Method method, Object[] args, Object target) throws Throwable;
}
