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
 * @date 2021/7/29 16:02
 * @email kfyty725@hotmail.com
 */
public interface MethodAfterAdvice extends Advice, MethodInterceptorChainPoint {

    @Override
    default Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        try {
            return chain.proceed(methodProxy);
        } finally {
            this.after(methodProxy.getTargetMethod(), methodProxy.getArguments(), methodProxy.getTarget());
        }
    }

    void after(Method method, Object[] args, Object target) throws Throwable;
}
