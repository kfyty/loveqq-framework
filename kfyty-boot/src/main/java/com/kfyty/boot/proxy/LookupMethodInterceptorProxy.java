package com.kfyty.boot.proxy;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Lookup;
import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.autoconfig.beans.autowired.AutowiredDescription;
import com.kfyty.core.autoconfig.beans.autowired.AutowiredProcessor;
import com.kfyty.core.generic.ActualGeneric;
import com.kfyty.core.proxy.MethodInterceptorChain;
import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.core.proxy.MethodProxy;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.BeanUtil;
import com.kfyty.core.utils.CommonUtil;

import java.lang.reflect.Method;

/**
 * 描述: Lookup 注解代理
 *
 * @author kfyty725
 * @date 2021/7/11 12:30
 * @email kfyty725@hotmail.com
 */
@Order(ConfigurationBeanInterceptorProxy.BEAN_METHOD_PROXY_ORDER)
public class LookupMethodInterceptorProxy implements MethodInterceptorChainPoint {
    private final AutowiredProcessor autowiredProcessor;

    public LookupMethodInterceptorProxy(ApplicationContext context) {
        this.autowiredProcessor = new AutowiredProcessor(context);
    }

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
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
