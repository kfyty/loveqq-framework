package com.kfyty.aop.aspectj;

import com.kfyty.aop.ThrowsAdvice;
import com.kfyty.core.utils.AnnotationUtil;
import org.aspectj.lang.annotation.AfterThrowing;

import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/7/31 16:12
 * @email kfyty725@hotmail.com
 */
public class AspectJAfterThrowsAdvice extends AbstractAspectJAdvice implements ThrowsAdvice {

    @Override
    public void afterThrowing(Method method, Object[] args, Object target, Throwable throwable) {
        this.invokeAdviceMethod(method, this.getJoinPoint(), null, throwable);
    }

    @Override
    protected void onSetPointcut(AspectJExpressionPointcut pointcut) {
        super.onSetPointcut(pointcut);
        AfterThrowing annotation = AnnotationUtil.findAnnotation(this.getAspectAdviceMethod(), AfterThrowing.class);
        this.setThrowing(annotation.throwing());
    }
}
