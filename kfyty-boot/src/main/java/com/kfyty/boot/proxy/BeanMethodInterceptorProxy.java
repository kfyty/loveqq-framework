package com.kfyty.boot.proxy;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxyWrapper;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.ScopeUtil;

import java.lang.reflect.Method;

/**
 * 描述: bean 注解代理
 *
 * @author kfyty725
 * @date 2021/6/13 17:30
 * @email kfyty725@hotmail.com
 */
@Order(BeanMethodInterceptorProxy.BEAN_METHOD_PROXY_ORDER)
public class BeanMethodInterceptorProxy implements InterceptorChainPoint {
    public static final int BEAN_METHOD_PROXY_ORDER = ScopeProxyInterceptorProxy.SCOPE_PROXY_ORDER >> 1;

    private final ApplicationContext context;

    public BeanMethodInterceptorProxy(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object proceed(MethodProxyWrapper methodProxy, MethodInterceptorChain chain) throws Throwable {
        Method method = methodProxy.getTargetMethod();
        Bean annotation = AnnotationUtil.findAnnotation(method, Bean.class);
        if (annotation == null || !ScopeUtil.isSingleton(method)) {
            return chain.proceed(methodProxy);
        }
        String beanName = BeanUtil.getBeanName(method, annotation);
        if (this.context.contains(beanName)) {
            return this.context.getBean(beanName);
        }
        BeanDefinition beanDefinition = this.context.getBeanDefinition(beanName, method.getReturnType());
        return this.context.registerBean(beanDefinition);
    }
}
