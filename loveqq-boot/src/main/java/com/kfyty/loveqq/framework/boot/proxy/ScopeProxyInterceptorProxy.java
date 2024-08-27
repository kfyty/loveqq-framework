package com.kfyty.loveqq.framework.boot.proxy;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;
import com.kfyty.loveqq.framework.core.autoconfig.scope.ScopeProxyFactory;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import lombok.RequiredArgsConstructor;

/**
 * 描述: 作用域代理
 *
 * @author kfyty725
 * @date 2021/7/11 12:30
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
@Order(Order.HIGHEST_PRECEDENCE)
public class ScopeProxyInterceptorProxy implements MethodInterceptorChainPoint, InternalPriority {
    private final BeanDefinition beanDefinition;
    private final BeanFactory beanFactory;
    private final ScopeProxyFactory scopeProxyFactory;

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        Object bean = null;
        try {
            methodProxy.setTarget((bean = this.scopeProxyFactory.getObject(this.beanDefinition, this.beanFactory)));
            return chain.proceed(methodProxy);
        } finally {
            if (bean != null && !this.beanDefinition.isSingleton()) {
                this.beanFactory.destroyBean(this.beanDefinition.getBeanName(), bean);
            }
        }
    }
}
