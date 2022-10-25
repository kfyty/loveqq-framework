package com.kfyty.aop.proxy;

import com.kfyty.aop.aspectj.MethodInvocationProceedingJoinPoint;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.proxy.MethodInterceptorChainPoint;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxy;
import org.aspectj.lang.JoinPoint;

/**
 * 描述: 暴露 MethodInvocationProceedingJoinPoint
 *
 * @author kfyty725
 * @date 2021/8/1 14:33
 * @email kfyty725@hotmail.com
 */
@Order(Integer.MIN_VALUE)
public class ExposeInvocationInterceptorProxy implements MethodInterceptorChainPoint {
    private static final ThreadLocal<JoinPoint> CURRENT_JOIN_POINT = new ThreadLocal<>();

    private final MethodInvocationProceedingJoinPoint joinPoint;

    public ExposeInvocationInterceptorProxy(MethodInvocationProceedingJoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    public static JoinPoint currentJoinPoint() {
        JoinPoint joinPoint = CURRENT_JOIN_POINT.get();
        if (joinPoint == null) {
            throw new IllegalStateException("No join point found !");
        }
        return joinPoint;
    }

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        JoinPoint oldJoinPoint = CURRENT_JOIN_POINT.get();
        try {
            CURRENT_JOIN_POINT.set(this.joinPoint);
            return chain.proceed(methodProxy);
        } finally {
            CURRENT_JOIN_POINT.set(oldJoinPoint);
        }
    }
}
