package com.kfyty.loveqq.framework.boot.processor.factory;

import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPreProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;
import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.isAbstract;

/**
 * 描述: 扫描 bean 定义
 *
 * @author kfyty725
 * @date 2022/10/29 15:23
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Integer.MIN_VALUE)
public class ScanBeanDefinitionBeanFactoryPreProcessor implements BeanFactoryPreProcessor, InternalPriority {

    @Override
    public boolean allowAutowired() {
        return false;
    }

    @Override
    public void preProcessBeanFactory(BeanFactory beanFactory) {
        if (beanFactory instanceof ConfigurableApplicationContext) {
            this.preProcessBeanFactory((ConfigurableApplicationContext) beanFactory);
        }
        beanFactory.resolveConditionBeanDefinitionRegistry();
    }

    protected void preProcessBeanFactory(ConfigurableApplicationContext applicationContext) {
        for (Class<?> scannedClass : applicationContext.getScannedClasses()) {
            if (BeanFactoryPreProcessor.class.isAssignableFrom(scannedClass)) {
                continue;
            }
            if (!isAbstract(scannedClass) && applicationContext.isMatchComponent(scannedClass)) {
                BeanDefinition beanDefinition = genericBeanDefinition(scannedClass).getBeanDefinition();
                applicationContext.registerBeanDefinition(beanDefinition);
            }
        }
    }
}
