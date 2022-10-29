package com.kfyty.boot.proxy;

import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.autoconfig.beans.ScopeProxyFactory;
import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.core.proxy.MethodInterceptorChain;
import com.kfyty.core.proxy.MethodProxy;
import lombok.RequiredArgsConstructor;

/**
 * 描述: 作用域代理
 *
 * @author kfyty725
 * @date 2021/7/11 12:30
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
@Order(ScopeProxyInterceptorProxy.SCOPE_PROXY_ORDER)
public class ScopeProxyInterceptorProxy implements MethodInterceptorChainPoint {
    public static final int SCOPE_PROXY_ORDER = -1 << 15;

    private final BeanDefinition beanDefinition;
    private final BeanFactory beanFactory;
    private final ScopeProxyFactory scopeProxyFactory;

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        String requiredBeanName = BeanMethodInterceptorProxy.getCurrentRequiredBeanName();
        try {
            BeanMethodInterceptorProxy.setCurrentRequiredBeanName(this.beanDefinition.getBeanName());
            methodProxy.setTarget(this.scopeProxyFactory.getObject(this.beanDefinition, this.beanFactory));
            return chain.proceed(methodProxy);
        } finally {
            BeanMethodInterceptorProxy.setCurrentRequiredBeanName(requiredBeanName);
        }
    }
}
