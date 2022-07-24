package com.kfyty.boot.proxy;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Lookup;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.autoconfig.beans.autowired.AutowiredDescription;
import com.kfyty.support.autoconfig.beans.autowired.AutowiredProcessor;
import com.kfyty.support.generic.ActualGeneric;
import com.kfyty.support.proxy.MethodInterceptorChain;
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
@Order(BeanMethodInterceptorProxy.BEAN_METHOD_PROXY_ORDER)
public class LookupMethodInterceptorProxy implements InterceptorChainPoint {
    private final AutowiredProcessor autowiredProcessor;

    public LookupMethodInterceptorProxy(ApplicationContext context) {
        this.autowiredProcessor = new AutowiredProcessor(context);
    }

    @Override
    public Object proceed(MethodProxyWrapper methodProxy, MethodInterceptorChain chain) throws Throwable {
        Method method = methodProxy.getTargetMethod();
        Lookup annotation = AnnotationUtil.findAnnotation(method, Lookup.class);
        if(annotation == null) {
            return chain.proceed(methodProxy);
        }
        String beanName = CommonUtil.notEmpty(annotation.value()) ? annotation.value() : BeanUtil.getBeanName(method.getReturnType());
        AutowiredDescription description = AutowiredDescription.from(AnnotationUtil.findAnnotation(method, Autowired.class));
        return this.autowiredProcessor.doResolveBean(beanName, ActualGeneric.from(method), description);
    }
}
