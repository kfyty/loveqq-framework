package com.kfyty.loveqq.framework.boot.processor;

import com.kfyty.loveqq.framework.boot.proxy.ConfigurationClassInterceptorProxy;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.lang.instrument.ClassFileTransformerClassLoader;
import com.kfyty.loveqq.framework.core.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.ClassLoaderUtil;

/**
 * 描述: {@link com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration} 提供自调用支持
 * 代理方式支持，当 {@link com.kfyty.loveqq.framework.boot.instrument.ConfigurationClassInstrument} 无效时将自动启用
 *
 * @author kfyty725
 * @date 2021/6/13 17:27
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Order.HIGHEST_PRECEDENCE)
public class ConfigurationClassPostProcessor extends AbstractProxyCreatorProcessor {

    @Override
    public boolean canCreateProxy(String beanName, Class<?> beanType, Object bean) {
        if (ClassLoaderUtil.isIndexedClassLoader() && ClassFileTransformerClassLoader.LOAD_TRANSFORMER) {
            return false;
        }
        return AnnotationUtil.hasAnnotationElement(beanType, Configuration.class);
    }

    @Override
    public MethodInterceptorChainPoint createProxyPoint() {
        return new ConfigurationClassInterceptorProxy(this.applicationContext);
    }
}
