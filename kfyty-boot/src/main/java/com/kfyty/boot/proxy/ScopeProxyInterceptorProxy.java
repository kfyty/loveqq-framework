package com.kfyty.boot.proxy;

import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.autoconfig.internal.InternalPriority;
import com.kfyty.core.autoconfig.scope.ScopeProxyFactory;
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
@Order(Order.HIGHEST_PRECEDENCE)
public class ScopeProxyInterceptorProxy implements MethodInterceptorChainPoint, InternalPriority {
    private final BeanDefinition beanDefinition;
    private final BeanFactory beanFactory;
    private final ScopeProxyFactory scopeProxyFactory;

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        Object bean = null;
        String requiredBeanName = ConfigurationBeanInterceptorProxy.getCurrentRequiredBeanName();
        try {
            ConfigurationBeanInterceptorProxy.setCurrentRequiredBeanName(this.beanDefinition.getBeanName());
            methodProxy.setTarget((bean = this.scopeProxyFactory.getObject(this.beanDefinition, this.beanFactory)));
            return chain.proceed(methodProxy);
        } finally {
            ConfigurationBeanInterceptorProxy.setCurrentRequiredBeanName(requiredBeanName);
            if (bean != null) {
                this.beanFactory.destroyBean(this.beanDefinition.getBeanName(), bean);
            }
        }
    }
}
