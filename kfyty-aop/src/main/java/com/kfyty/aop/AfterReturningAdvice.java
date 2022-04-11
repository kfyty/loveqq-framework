package com.kfyty.aop;

import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxyWrapper;
import org.aopalliance.aop.Advice;

import java.lang.reflect.Method;

/**
 * 描述: 方法后置通知
 *
 * @author kfyty725
 * @date 2021/7/29 16:03
 * @email kfyty725@hotmail.com
 */
public interface AfterReturningAdvice extends Advice, InterceptorChainPoint {

    @Override
    default Object proceed(MethodProxyWrapper methodProxy, MethodInterceptorChain chain) throws Throwable {
        Object retValue = chain.proceed(methodProxy);
        this.afterReturning(retValue, methodProxy.getTargetMethod(), methodProxy.getArguments(), methodProxy.getTarget());
        return retValue;
    }

    void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable;
}
