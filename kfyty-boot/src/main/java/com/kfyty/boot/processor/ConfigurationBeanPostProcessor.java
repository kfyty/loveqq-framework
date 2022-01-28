package com.kfyty.boot.processor;

import com.kfyty.boot.proxy.BeanMethodInterceptorProxy;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
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
@Order(Order.HIGHEST_PRECEDENCE)
public class ConfigurationBeanPostProcessor extends AbstractProxyCreatorProcessor {

    @Override
    public boolean canCreateProxy(Object bean, String beanName) {
        Class<?> sourceClass = AopUtil.getSourceClass(bean);
        return AnnotationUtil.hasAnnotationElement(sourceClass, Configuration.class);
    }

    @Override
    public InterceptorChainPoint createProxyPoint() {
        return new BeanMethodInterceptorProxy(this.applicationContext);
    }
}
