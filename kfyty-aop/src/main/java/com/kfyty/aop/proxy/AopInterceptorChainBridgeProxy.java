package com.kfyty.aop.proxy;

import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.core.proxy.MethodInterceptorChain;
import com.kfyty.core.proxy.MethodProxy;
import lombok.RequiredArgsConstructor;

/**
 * 描述: aop 拦截链桥接代理
 *
 * @author kfyty725
 * @date 2021/8/1 13:59
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
@Order(Integer.MAX_VALUE)
public class AopInterceptorChainBridgeProxy implements MethodInterceptorChainPoint {
    private final MethodInterceptorChain sourceChain;

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        return this.sourceChain.proceed(methodProxy);
    }
}
