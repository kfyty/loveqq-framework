package com.kfyty.aop.aspectj;

import com.kfyty.aop.MethodAfterAdvice;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxyWrapper;

import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/7/31 16:11
 * @email kfyty725@hotmail.com
 */
public class AspectJMethodAfterAdvice extends AbstractAspectJAdvice implements MethodAfterAdvice {

    @Override
    public void after(Method method, Object[] args, Object target) throws Throwable {
        this.invokeAdviceMethod(this.getJoinPoint(), null, null);
    }

    @Override
    public Object proceed(MethodProxyWrapper methodProxy, MethodInterceptorChain chain) throws Throwable {
        try {
            return chain.proceed(methodProxy);
        } finally {
            this.after(methodProxy.getMethod(), methodProxy.getArguments(), methodProxy.getSource());
        }
    }
}
