package com.kfyty.support.proxy;

/**
 * 描述: 方法拦截链节点
 *
 * @author kfyty725
 * @date 2021/6/19 11:12
 * @email kfyty725@hotmail.com
 */
public interface InterceptorChainPoint {
    Object proceed(MethodProxyWrap methodProxy, InterceptorChain chain) throws Throwable;
}
