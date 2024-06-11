package com.kfyty.loveqq.framework.core.proxy.aop;

import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;

/**
 * 描述: 支持获取通知类型的方法拦截器点
 *
 * @author kfyty725
 * @date 2022/3/20 16:47
 * @email kfyty725@hotmail.com
 */
public interface AdviceMethodInterceptorChainPoint extends MethodInterceptorChainPoint {
    /**
     * 返回通知类型
     */
    Class<?> getAdviceType();
}
