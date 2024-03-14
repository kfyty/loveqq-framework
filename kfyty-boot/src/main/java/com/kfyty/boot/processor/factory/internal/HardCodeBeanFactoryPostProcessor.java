package com.kfyty.boot.processor.factory.internal;

import com.kfyty.boot.processor.factory.FactoryBeanBeanFactoryPostProcessor;
import com.kfyty.boot.processor.factory.LazyProxyBeanFactoryPostProcessor;
import com.kfyty.boot.processor.factory.ScopeProxyBeanFactoryPostProcessor;
import com.kfyty.core.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.beans.BeanFactory;

/**
 * 描述: 硬编码后置处理器，主要用于执行特定顺序的处理器
 *
 * @author kfyty725
 * @date 2022/10/23 15:30
 * @email kfyty725@hotmail.com
 */
@Component
public class HardCodeBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Autowired
    protected ScopeProxyBeanFactoryPostProcessor scopeProxyBeanFactoryPostProcessor;

    @Autowired
    protected LazyProxyBeanFactoryPostProcessor lazyProxyBeanFactoryPostProcessor;

    @Autowired
    protected FactoryBeanBeanFactoryPostProcessor factoryBeanBeanFactoryPostProcessor;

    @Override
    public void postProcessBeanFactory(BeanFactory beanFactory) {
        this.scopeProxyBeanFactoryPostProcessor.postProcessBeanFactory(beanFactory);
        this.lazyProxyBeanFactoryPostProcessor.postProcessBeanFactory(beanFactory);
        this.factoryBeanBeanFactoryPostProcessor.postProcessBeanFactory(beanFactory);
    }
}
