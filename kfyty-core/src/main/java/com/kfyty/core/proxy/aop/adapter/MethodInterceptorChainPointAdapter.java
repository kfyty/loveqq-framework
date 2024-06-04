package com.kfyty.core.proxy.aop.adapter;

import com.kfyty.core.proxy.MethodInterceptorChain;
import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.core.proxy.MethodProxy;
import com.kfyty.core.proxy.aop.AdviceMethodInterceptorChainPoint;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * 描述: {@link MethodInterceptor} 与 {@link MethodInterceptorChainPoint} 适配器
 *
 * @author kfyty725
 * @date 2022/3/20 16:47
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class MethodInterceptorChainPointAdapter implements AdviceMethodInterceptorChainPoint {
    private final MethodInterceptor methodInterceptor;

    @Override
    public Class<?> getAdviceType() {
        return this.methodInterceptor.getClass();
    }

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        return this.methodInterceptor.invoke(new MethodProxyInvocationAdapter(methodProxy, chain));
    }
}
