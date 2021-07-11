package com.kfyty.boot.proxy;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Lookup;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.autoconfig.beans.AutowiredProcessor;
import com.kfyty.support.generic.ActualGeneric;
import com.kfyty.support.proxy.InterceptorChain;
import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.proxy.MethodProxyWrapper;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.CommonUtil;

import java.lang.reflect.Method;

/**
 * 描述: Lookup 注解代理
 *
 * @author kfyty725
 * @date 2021/7/11 12:30
 * @email kfyty725@hotmail.com
 */
@Order(0)
public class LookupMethodInterceptorProxy implements InterceptorChainPoint {
    private final AutowiredProcessor autowiredProcessor;

    public LookupMethodInterceptorProxy(ApplicationContext context) {
        this.autowiredProcessor = new AutowiredProcessor(context);
    }

    @Override
    public Object proceed(MethodProxyWrapper methodProxy, InterceptorChain chain) throws Throwable {
        Method method = methodProxy.getSourceMethod();
        Lookup annotation = AnnotationUtil.findAnnotation(method, Lookup.class);
        if(annotation == null) {
            return chain.proceed(methodProxy);
        }
        String beanName = CommonUtil.notEmpty(annotation.value()) ? annotation.value() : BeanUtil.convert2BeanName(method.getReturnType());
        return this.autowiredProcessor.doResolveBean(beanName, ActualGeneric.from(method), AnnotationUtil.findAnnotation(method, Autowired.class));
    }
}
