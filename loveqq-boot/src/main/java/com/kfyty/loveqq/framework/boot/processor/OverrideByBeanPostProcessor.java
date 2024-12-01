package com.kfyty.loveqq.framework.boot.processor;

import com.kfyty.loveqq.framework.boot.proxy.OverrideByProxyInterceptorProxy;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.delegate.By;
import com.kfyty.loveqq.framework.core.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
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
public class OverrideByBeanPostProcessor extends AbstractProxyCreatorProcessor {

    @Override
    public boolean canCreateProxy(String beanName, Class<?> beanType, Object bean) {
        return By.class.isAssignableFrom(beanType);
    }

    @Override
    public MethodInterceptorChainPoint createProxyPoint() {
        return new OverrideByProxyInterceptorProxy(this.applicationContext);
    }
}
