package com.kfyty.loveqq.framework.boot.processor;

import com.kfyty.loveqq.framework.boot.proxy.ConfigurationBeanInterceptorProxy;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/13 17:27
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Order.HIGHEST_PRECEDENCE)
public class ConfigurationBeanPostProcessor extends AbstractProxyCreatorProcessor {

    @Override
    public boolean canCreateProxy(String beanName, Class<?> beanType, Object bean) {
        return AnnotationUtil.hasAnnotationElement(beanType, Configuration.class);
    }

    @Override
    public MethodInterceptorChainPoint createProxyPoint() {
        return new ConfigurationBeanInterceptorProxy(this.applicationContext);
    }
}
