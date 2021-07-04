package com.kfyty.boot.proxy;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.proxy.InterceptorChain;
import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.proxy.MethodProxyWrap;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;

import java.lang.reflect.Method;

/**
 * 描述: bean 注解代理
 *
 * @author kfyty725
 * @date 2021/6/13 17:30
 * @email kfyty725@hotmail.com
 */
public class BeanMethodInterceptorProxy implements InterceptorChainPoint {
    private final ApplicationContext context;

    public BeanMethodInterceptorProxy(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object proceed(MethodProxyWrap methodProxy, InterceptorChain chain) throws Throwable {
        Method method = methodProxy.getSourceMethod();
        Bean annotation = AnnotationUtil.findAnnotation(method, Bean.class);
        if(annotation == null) {
            return chain.proceed(methodProxy);
        }
        String beanName = BeanUtil.getBeanName(method, annotation);
        if(this.context.contains(beanName)) {
            return this.context.getBean(beanName);
        }
        BeanDefinition beanDefinition = this.context.getBeanDefinition(beanName, method.getReturnType());
        return this.context.registerBean(beanDefinition);
    }
}
