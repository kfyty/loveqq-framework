package com.kfyty.loveqq.framework.boot.processor;

import com.kfyty.loveqq.framework.boot.proxy.LookupMethodInterceptorProxy;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Lookup;
import com.kfyty.loveqq.framework.core.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotationElement;
import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.getMethods;
import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.isAbstract;

/**
 * 描述: 处理非抽象 bean
 *
 * @author kfyty725
 * @date 2021/6/13 17:27
 * @email kfyty725@hotmail.com
 */
@Component
public class LookupMethodBeanPostProcessor extends AbstractProxyCreatorProcessor {

    @Override
    public boolean canCreateProxy(String beanName, Class<?> beanType, Object bean) {
        return !isAbstract(beanType) && getMethods(beanType).stream().anyMatch(e -> hasAnnotationElement(e, Lookup.class));
    }

    @Override
    public MethodInterceptorChainPoint createProxyPoint() {
        return new LookupMethodInterceptorProxy(this.applicationContext);
    }
}
