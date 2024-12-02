package com.kfyty.loveqq.framework.boot.processor;

import com.kfyty.loveqq.framework.boot.proxy.DelegateProxyInterceptorProxy;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.delegate.Delegate;
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
public class DelegateBeanPostProcessor extends AbstractProxyCreatorProcessor {

    @Override
    public boolean canCreateProxy(String beanName, Class<?> beanType, Object bean) {
        return Delegate.class.isAssignableFrom(beanType);
    }

    @Override
    public MethodInterceptorChainPoint createProxyPoint() {
        return new DelegateProxyInterceptorProxy(this.applicationContext);
    }
}
