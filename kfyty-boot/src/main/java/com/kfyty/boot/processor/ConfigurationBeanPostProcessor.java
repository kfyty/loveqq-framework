package com.kfyty.boot.processor;

import com.kfyty.boot.proxy.BeanMethodInterceptorProxy;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.support.proxy.MethodInterceptorChainPoint;
import com.kfyty.support.utils.AnnotationUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/13 17:27
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Component
@Order(Order.HIGHEST_PRECEDENCE)
public class ConfigurationBeanPostProcessor extends AbstractProxyCreatorProcessor {

    @Override
    public boolean canCreateProxy(String beanName, Class<?> beanType, Object bean) {
        return AnnotationUtil.hasAnnotationElement(beanType, Configuration.class);
    }

    @Override
    public MethodInterceptorChainPoint createProxyPoint() {
        return new BeanMethodInterceptorProxy(this.applicationContext);
    }
}
