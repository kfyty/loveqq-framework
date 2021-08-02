package com.kfyty.aop;

/**
 * 描述: PointcutAdvisor
 *
 * @author kfyty725
 * @date 2021/7/30 12:30
 * @email kfyty725@hotmail.com
 */
public interface PointcutAdvisor extends Advisor {
    Pointcut getPointcut();
}
