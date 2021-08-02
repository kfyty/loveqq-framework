package com.kfyty.aop;

/**
 * 描述: 切入点
 *
 * @author kfyty725
 * @date 2021/7/29 11:30
 * @email kfyty725@hotmail.com
 */
public interface Pointcut {
    MethodMatcher getMethodMatcher();
}
