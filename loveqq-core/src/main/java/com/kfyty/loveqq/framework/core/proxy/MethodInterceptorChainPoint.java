package com.kfyty.loveqq.framework.core.proxy;

/**
 * 描述: 方法拦截链节点
 *
 * @author kfyty725
 * @date 2021/6/19 11:12
 * @email kfyty725@hotmail.com
 */
public interface MethodInterceptorChainPoint {
    /**
     * 方法代理拦截点
     *
     * @param methodProxy 方法代理
     * @param chain       方法拦截链
     * @return 结果
     */
    Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable;
}
