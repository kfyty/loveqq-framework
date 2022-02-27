package com.kfyty.aop.aspectj;

import com.kfyty.aop.AfterReturningAdvice;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxyWrapper;
import com.kfyty.support.utils.AnnotationUtil;
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
    public Object proceed(MethodProxyWrapper methodProxy, MethodInterceptorChain chain) throws Throwable {
        Object retValue = chain.proceed(methodProxy);
        this.afterReturning(retValue, methodProxy.getTargetMethod(), methodProxy.getArguments(), methodProxy.getTarget());
        return retValue;
    }

    @Override
    protected void onSetPointcut(AspectJExpressionPointcut pointcut) {
        super.onSetPointcut(pointcut);
        AfterReturning annotation = AnnotationUtil.findAnnotation(this.getAspectAdviceMethod(), AfterReturning.class);
        this.setReturning(annotation.returning());
    }
}
