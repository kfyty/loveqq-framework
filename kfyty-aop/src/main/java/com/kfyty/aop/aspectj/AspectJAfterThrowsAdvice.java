package com.kfyty.aop.aspectj;

import com.kfyty.aop.ThrowsAdvice;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxyWrapper;
import com.kfyty.support.utils.AnnotationUtil;
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
    public Object proceed(MethodProxyWrapper methodProxy, MethodInterceptorChain chain) throws Throwable {
        try {
            return chain.proceed(methodProxy);
        } catch (Throwable throwable) {
            this.afterThrowing(methodProxy.getSourceTargetMethod(), methodProxy.getArguments(), methodProxy.getSource(), throwable);
            throw throwable;
        }
    }

    @Override
    protected void onSetPointcut(AspectJExpressionPointcut pointcut) {
        super.onSetPointcut(pointcut);
        AfterThrowing annotation = AnnotationUtil.findAnnotation(this.getAspectAdviceMethod(), AfterThrowing.class);
        this.setThrowing(annotation.throwing());
    }
}
