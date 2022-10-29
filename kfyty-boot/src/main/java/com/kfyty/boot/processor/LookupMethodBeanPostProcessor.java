package com.kfyty.boot.processor;

import com.kfyty.boot.proxy.LookupMethodInterceptorProxy;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.Lookup;
import com.kfyty.core.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 描述: 处理非抽象 bean
 *
 * @author kfyty725
 * @date 2021/6/13 17:27
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Component
public class LookupMethodBeanPostProcessor extends AbstractProxyCreatorProcessor {

    @Override
    public boolean canCreateProxy(String beanName, Class<?> beanType, Object bean) {
        return !ReflectUtil.isAbstract(this.getBeanDefinition(beanName).getBeanType()) && ReflectUtil.getMethods(beanType).stream().anyMatch(e -> AnnotationUtil.hasAnnotation(e, Lookup.class));
    }

    @Override
    public MethodInterceptorChainPoint createProxyPoint() {
        return new LookupMethodInterceptorProxy(this.applicationContext);
    }
}
