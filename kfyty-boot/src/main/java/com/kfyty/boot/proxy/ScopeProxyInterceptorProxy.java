package com.kfyty.boot.proxy;

import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.BeanFactory;
import com.kfyty.support.proxy.InterceptorChain;
import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.proxy.MethodProxyWrapper;
import com.kfyty.support.utils.ReflectUtil;

/**
 * 描述: 作用域代理
 *
 * @author kfyty725
 * @date 2021/7/11 12:30
 * @email kfyty725@hotmail.com
 */
@Order(ScopeProxyInterceptorProxy.SCOPE_PROXY_ORDER)
public class ScopeProxyInterceptorProxy implements InterceptorChainPoint {
    public static final int SCOPE_PROXY_ORDER = -1;

    private final BeanFactory beanFactory;
    private final BeanDefinition sourceBeanDefinition;

    public ScopeProxyInterceptorProxy(BeanFactory beanFactory, BeanDefinition beanDefinition) {
        this.beanFactory = beanFactory;
        this.sourceBeanDefinition = beanDefinition;
    }

    @Override
    public Object proceed(MethodProxyWrapper methodProxy, InterceptorChain chain) throws Throwable {
        Object bean = this.beanFactory.registerBean(this.sourceBeanDefinition);
        return ReflectUtil.invokeMethod(bean, methodProxy.getSourceMethod(), methodProxy.getArgs());
    }
}
