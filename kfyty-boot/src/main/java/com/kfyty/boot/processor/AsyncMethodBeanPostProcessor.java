package com.kfyty.boot.processor;

import com.kfyty.boot.proxy.AsyncMethodInterceptorProxy;
import com.kfyty.support.autoconfig.annotation.Async;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/13 17:27
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Configuration
public class AsyncMethodBeanPostProcessor extends AbstractProxyCreatorProcessor {

    @Override
    public boolean canCreateProxy(Object bean, String beanName) {
        Class<?> sourceClass = AopUtil.getTargetClass(bean);
        return AnnotationUtil.hasAnnotation(sourceClass, Async.class) || ReflectUtil.getMethods(sourceClass).stream().anyMatch(e -> AnnotationUtil.hasAnnotation(e, Async.class));
    }

    @Override
    public InterceptorChainPoint createProxyPoint() {
        return new AsyncMethodInterceptorProxy(this.applicationContext);
    }
}
