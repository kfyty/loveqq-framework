package com.kfyty.loveqq.framework.core.proxy;

/**
 * 描述: 方法拦截链节点
 *
 * @author kfyty725
 * @date 2021/6/19 11:12
 * @email kfyty725@hotmail.com
 */
public interface MethodInterceptorChainPoint {
    Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable;
}
