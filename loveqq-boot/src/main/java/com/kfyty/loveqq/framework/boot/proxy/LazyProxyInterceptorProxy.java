package com.kfyty.loveqq.framework.boot.proxy;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

/**
 * 描述: 延迟初始化代理
 *
 * @author kfyty725
 * @date 2021/7/11 12:30
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class LazyProxyInterceptorProxy implements MethodInterceptorChainPoint {
    private final String beanName;
    private final BeanFactory beanFactory;

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        if (!this.beanFactory.contains(beanName) && this.isToStringHashCode(methodProxy)) {
            return chain.proceed(methodProxy);
        }
        String requiredBeanName = ConfigurationBeanInterceptorProxy.getCurrentRequiredBeanName();
        try {
            ConfigurationBeanInterceptorProxy.setCurrentRequiredBeanName(this.beanName);
            methodProxy.setTarget(this.beanFactory.getBean(this.beanName));
            return chain.proceed(methodProxy);
        } finally {
            ConfigurationBeanInterceptorProxy.setCurrentRequiredBeanName(requiredBeanName);
        }
    }

    protected boolean isToStringHashCode(MethodProxy methodProxy) {
        Method method = methodProxy.getTargetMethod();
        if (method.getName().equals("toString") && method.getParameterCount() == 0) {
            return true;
        }
        if (method.getName().equals("hashCode") && method.getParameterCount() == 0) {
            return true;
        }
        return false;
    }
}
