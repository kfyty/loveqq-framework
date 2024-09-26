package com.kfyty.loveqq.framework.core.proxy.aop.adapter;

import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import com.kfyty.loveqq.framework.core.proxy.aop.AdviceMethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
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
    public Class<? extends MethodInterceptor> getAdviceType() {
        return this.methodInterceptor.getClass();
    }

    @Override
    public int getOrder() {
        return BeanUtil.getBeanOrder(this.methodInterceptor);
    }

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        return this.methodInterceptor.invoke(new MethodProxyInvocationAdapter(methodProxy, chain));
    }
}
