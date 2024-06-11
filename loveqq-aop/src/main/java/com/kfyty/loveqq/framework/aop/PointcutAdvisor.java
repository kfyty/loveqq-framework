package com.kfyty.loveqq.framework.aop;

/**
 * 描述: PointcutAdvisor
 *
 * @author kfyty725
 * @date 2021/7/30 12:30
 * @email kfyty725@hotmail.com
 */
public interface PointcutAdvisor extends Advisor {
    /**
     * 获取切入点
     *
     * @return 切入点
     */
    Pointcut getPointcut();
}
