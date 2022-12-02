package com.kfyty.aop;

/**
 * 描述: 切入点
 *
 * @author kfyty725
 * @date 2021/7/29 11:30
 * @email kfyty725@hotmail.com
 */
public interface Pointcut {
    /**
     * 获取方法匹配器
     *
     * @return 方法匹配器
     */
    MethodMatcher getMethodMatcher();
}
