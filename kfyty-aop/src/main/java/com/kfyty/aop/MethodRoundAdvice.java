package com.kfyty.aop;

import org.aopalliance.aop.Advice;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 描述: 方法环绕通知
 *
 * @author kfyty725
 * @date 2021/7/29 16:04
 * @email kfyty725@hotmail.com
 */
public interface MethodRoundAdvice extends Advice {
    Object around(ProceedingJoinPoint pjp) throws Throwable;
}
