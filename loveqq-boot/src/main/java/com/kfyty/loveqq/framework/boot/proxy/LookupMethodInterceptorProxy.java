package com.kfyty.loveqq.framework.boot.proxy;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Lookup;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.AutowiredDescription;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.AutowiredProcessor;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.lang.reflect.Method;

/**
 * 描述: Lookup 注解代理
 *
 * @author kfyty725
 * @date 2021/7/11 12:30
 * @email kfyty725@hotmail.com
 */
public class LookupMethodInterceptorProxy implements MethodInterceptorChainPoint {
    private final AutowiredProcessor autowiredProcessor;

    public LookupMethodInterceptorProxy(ApplicationContext context) {
        this.autowiredProcessor = new AutowiredProcessor(context);
    }

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        Method method = methodProxy.getTargetMethod();
        Lookup annotation = AnnotationUtil.findAnnotation(method, Lookup.class);
        if (annotation == null) {
            return chain.proceed(methodProxy);
        }
        String beanName = CommonUtil.notEmpty(annotation.value()) ? annotation.value() : BeanUtil.getBeanName(method.getReturnType());
        AutowiredDescription description = this.autowiredProcessor.getResolver().resolve(method);
        return this.autowiredProcessor.doResolveBean(beanName, SimpleGeneric.from(method), description);
    }
}
