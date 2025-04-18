package com.kfyty.loveqq.framework.boot.processor.factory;

import com.kfyty.loveqq.framework.boot.processor.ConfigurationClassPostProcessor;
import com.kfyty.loveqq.framework.boot.processor.ConfigurationPropertiesBeanPostProcessor;
import com.kfyty.loveqq.framework.boot.processor.ValueAnnotationBeanPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPreProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;

/**
 * 描述: 初始化 bean 工厂，主要是注册内部 bean 后置处理器
 *
 * @author kfyty725
 * @date 2022/10/29 15:23
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Integer.MAX_VALUE)
public class InitializingBeanFactoryPreProcessor implements BeanFactoryPreProcessor, InternalPriority {

    @Override
    public boolean allowAutowired() {
        return false;
    }

    @Override
    public void preProcessBeanFactory(BeanFactory beanFactory) {
        ConfigurationClassPostProcessor configurationClassPostProcessor = beanFactory.getBean(ConfigurationClassPostProcessor.class);
        beanFactory.registerBeanPostProcessors(ConfigurationClassPostProcessor.class.getName(), configurationClassPostProcessor);

        ValueAnnotationBeanPostProcessor valueAnnotationBeanPostProcessor = beanFactory.getBean(ValueAnnotationBeanPostProcessor.class);
        beanFactory.registerBeanPostProcessors(ValueAnnotationBeanPostProcessor.class.getName(), valueAnnotationBeanPostProcessor);

        ConfigurationPropertiesBeanPostProcessor configurationPropertiesBeanPostProcessor = beanFactory.getBean(ConfigurationPropertiesBeanPostProcessor.class);
        beanFactory.registerBeanPostProcessors(ConfigurationPropertiesBeanPostProcessor.class.getName(), configurationPropertiesBeanPostProcessor);
    }
}
