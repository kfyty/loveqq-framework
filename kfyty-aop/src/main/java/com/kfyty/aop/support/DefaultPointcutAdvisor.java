package com.kfyty.aop.support;

import com.kfyty.aop.Pointcut;
import com.kfyty.aop.PointcutAdvisor;
import org.aopalliance.aop.Advice;

/**
 * 描述: 默认实现
 *
 * @author kfyty725
 * @date 2021/7/30 12:36
 * @email kfyty725@hotmail.com
 */
public class DefaultPointcutAdvisor implements PointcutAdvisor {
    private final Pointcut pointcut;
    private final Advice advice;

    public DefaultPointcutAdvisor(Pointcut pointcut, Advice advice) {
        this.pointcut = pointcut;
        this.advice = advice;
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }
}
