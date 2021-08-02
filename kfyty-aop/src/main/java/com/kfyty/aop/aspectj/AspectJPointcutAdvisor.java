package com.kfyty.aop.aspectj;

import com.kfyty.aop.Pointcut;
import com.kfyty.aop.PointcutAdvisor;
import org.aopalliance.aop.Advice;

/**
 * 描述: aspectJ 注解实现
 *
 * @author kfyty725
 * @date 2021/7/30 12:36
 * @email kfyty725@hotmail.com
 */
public class AspectJPointcutAdvisor implements PointcutAdvisor {
    private final Pointcut pointcut;
    private final AbstractAspectJAdvice advice;

    public AspectJPointcutAdvisor(Pointcut pointcut, AbstractAspectJAdvice advice) {
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
