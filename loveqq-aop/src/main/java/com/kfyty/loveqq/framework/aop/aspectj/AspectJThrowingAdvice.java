package com.kfyty.loveqq.framework.aop.aspectj;

import com.kfyty.loveqq.framework.core.proxy.aop.ThrowingAdvice;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import org.aspectj.lang.annotation.AfterThrowing;

import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/7/31 16:12
 * @email kfyty725@hotmail.com
 */
public class AspectJThrowingAdvice extends AbstractAspectJAdvice implements ThrowingAdvice {

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
