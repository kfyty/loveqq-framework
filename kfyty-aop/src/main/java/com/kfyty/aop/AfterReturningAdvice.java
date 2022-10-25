package com.kfyty.aop;

import com.kfyty.support.proxy.MethodInterceptorChainPoint;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxy;
import org.aopalliance.aop.Advice;

import java.lang.reflect.Method;

/**
 * 描述: 方法后置通知
 *
 * @author kfyty725
 * @date 2021/7/29 16:03
 * @email kfyty725@hotmail.com
 */
public interface AfterReturningAdvice extends Advice, MethodInterceptorChainPoint {

    @Override
    default Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        Object retValue = chain.proceed(methodProxy);
        this.afterReturning(retValue, methodProxy.getTargetMethod(), methodProxy.getArguments(), methodProxy.getTarget());
        return retValue;
    }

    void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable;
}
