package com.kfyty.aop.proxy;

import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxyWrapper;

/**
 * 描述: aop 拦截链桥接代理
 *
 * @author kfyty725
 * @date 2021/8/1 13:59
 * @email kfyty725@hotmail.com
 */
@Order(Integer.MAX_VALUE)
public class AopInterceptorChainBridgeProxy implements InterceptorChainPoint {
    private final MethodInterceptorChain sourceChain;

    public AopInterceptorChainBridgeProxy(MethodInterceptorChain sourceChain) {
        this.sourceChain = sourceChain;
    }

    @Override
    public Object proceed(MethodProxyWrapper methodProxy, MethodInterceptorChain chain) throws Throwable {
        return this.sourceChain.proceed(methodProxy);
    }
}
