package com.kfyty.aop;

import org.aopalliance.aop.Advice;

import java.lang.reflect.Method;

/**
 * 描述: 异常通知
 *
 * @author kfyty725
 * @date 2021/7/29 16:04
 * @email kfyty725@hotmail.com
 */
public interface ThrowsAdvice extends Advice {
    void afterThrowing(Method method, Object[] args, Object target, Throwable throwable);
}
