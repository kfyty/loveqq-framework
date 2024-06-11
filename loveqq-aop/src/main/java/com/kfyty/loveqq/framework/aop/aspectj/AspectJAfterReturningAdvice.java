package com.kfyty.loveqq.framework.aop.aspectj;

import com.kfyty.loveqq.framework.core.proxy.aop.AfterReturningAdvice;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import org.aspectj.lang.annotation.AfterReturning;

import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/7/31 16:12
 * @email kfyty725@hotmail.com
 */
public class AspectJAfterReturningAdvice extends AbstractAspectJAdvice implements AfterReturningAdvice {

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        this.invokeAdviceMethod(method, this.getJoinPoint(), returnValue, null);
    }

    @Override
    protected void onSetPointcut(AspectJExpressionPointcut pointcut) {
        super.onSetPointcut(pointcut);
        AfterReturning annotation = AnnotationUtil.findAnnotation(this.getAspectAdviceMethod(), AfterReturning.class);
        this.setReturning(annotation.returning());
    }
}
