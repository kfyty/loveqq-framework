package com.kfyty.aop.proxy;

import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.proxy.MethodInterceptorChainPoint;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxy;

/**
 * 描述: aop 拦截链桥接代理
 *
 * @author kfyty725
 * @date 2021/8/1 13:59
 * @email kfyty725@hotmail.com
 */
@Order(Integer.MAX_VALUE)
public class AopInterceptorChainBridgeProxy implements MethodInterceptorChainPoint {
    private final MethodInterceptorChain sourceChain;

    public AopInterceptorChainBridgeProxy(MethodInterceptorChain sourceChain) {
        this.sourceChain = sourceChain;
    }

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        return this.sourceChain.proceed(methodProxy);
    }
}
