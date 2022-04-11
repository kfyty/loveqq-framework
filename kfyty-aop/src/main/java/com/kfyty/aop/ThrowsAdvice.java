package com.kfyty.aop;

import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxyWrapper;
import org.aopalliance.aop.Advice;

import java.lang.reflect.Method;

/**
 * 描述: 异常通知
 *
 * @author kfyty725
 * @date 2021/7/29 16:04
 * @email kfyty725@hotmail.com
 */
public interface ThrowsAdvice extends Advice, InterceptorChainPoint {

    @Override
    default Object proceed(MethodProxyWrapper methodProxy, MethodInterceptorChain chain) throws Throwable {
        try {
            return chain.proceed(methodProxy);
        } catch (Throwable throwable) {
            this.afterThrowing(methodProxy.getTargetMethod(), methodProxy.getArguments(), methodProxy.getTarget(), throwable);
            throw throwable;
        }
    }

    void afterThrowing(Method method, Object[] args, Object target, Throwable throwable);
}
