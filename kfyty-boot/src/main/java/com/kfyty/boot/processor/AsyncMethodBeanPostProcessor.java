package com.kfyty.boot.processor;

import com.kfyty.boot.proxy.AsyncMethodInterceptorProxy;
import com.kfyty.core.autoconfig.annotation.Async;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import lombok.extern.slf4j.Slf4j;

import static com.kfyty.core.utils.AnnotationUtil.hasAnnotationElement;
import static com.kfyty.core.utils.ReflectUtil.getMethods;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/13 17:27
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Component
public class AsyncMethodBeanPostProcessor extends AbstractProxyCreatorProcessor {

    @Override
    public boolean canCreateProxy(String beanName, Class<?> beanType, Object bean) {
        return hasAnnotationElement(beanType, Async.class) || getMethods(beanType).stream().anyMatch(e -> hasAnnotationElement(e, Async.class));
    }

    @Override
    public MethodInterceptorChainPoint createProxyPoint() {
        return new AsyncMethodInterceptorProxy(this.applicationContext);
    }
}
