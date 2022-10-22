package com.kfyty.boot.proxy;

import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.BeanFactory;
import com.kfyty.support.autoconfig.beans.ScopeProxyFactory;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.proxy.MethodProxyWrapper;
import com.kfyty.support.utils.ReflectUtil;
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
public class ScopeProxyInterceptorProxy implements InterceptorChainPoint {
    public static final int SCOPE_PROXY_ORDER = -1 << 15;

    private final BeanDefinition beanDefinition;
    private final BeanFactory beanFactory;
    private final ScopeProxyFactory scopeProxyFactory;

    @Override
    public Object proceed(MethodProxyWrapper methodProxy, MethodInterceptorChain chain) throws Throwable {
        Object bean = this.scopeProxyFactory.getObject(this.beanDefinition, this.beanFactory);
        return ReflectUtil.invokeMethod(bean, methodProxy.getMethod(), methodProxy.getArguments());
    }
}
