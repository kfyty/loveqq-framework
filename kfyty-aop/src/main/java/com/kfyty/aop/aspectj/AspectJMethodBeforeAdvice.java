package com.kfyty.aop.aspectj;

import com.kfyty.aop.MethodBeforeAdvice;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxyWrapper;

import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/7/31 16:10
 * @email kfyty725@hotmail.com
 */
public class AspectJMethodBeforeAdvice extends AbstractAspectJAdvice implements MethodBeforeAdvice {

    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        this.invokeAdviceMethod(method, this.getJoinPoint(), null, null);
    }

    @Override
    public Object proceed(MethodProxyWrapper methodProxy, MethodInterceptorChain chain) throws Throwable {
        this.before(methodProxy.getSourceTargetMethod(), methodProxy.getArguments(), methodProxy.getSource());
        return chain.proceed(methodProxy);
    }
}
