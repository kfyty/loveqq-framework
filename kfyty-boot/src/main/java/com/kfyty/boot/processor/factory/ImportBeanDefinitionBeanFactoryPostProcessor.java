package com.kfyty.boot.processor.factory;

import com.kfyty.boot.context.AbstractApplicationContext;
import com.kfyty.support.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.support.autoconfig.ImportBeanDefinition;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.BeanFactory;

import java.util.Map;

/**
 * 描述: 导入自定义 bean 定义
 *
 * @author kfyty
 * @date 2022/10/23 15:30
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Order.HIGHEST_PRECEDENCE)
public class ImportBeanDefinitionBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(BeanFactory beanFactory) {
        if (beanFactory instanceof AbstractApplicationContext) {
            AbstractApplicationContext applicationContext = (AbstractApplicationContext) beanFactory;
            Map<String, BeanDefinition> importBeanDefines = beanFactory.getBeanDefinitions(e -> ImportBeanDefinition.class.isAssignableFrom(e.getValue().getBeanType()));
            for (BeanDefinition importBeanDefine : importBeanDefines.values()) {
                ImportBeanDefinition bean = (ImportBeanDefinition) beanFactory.registerBean(importBeanDefine);
                bean.doImport(applicationContext, applicationContext.getScanClasses()).forEach(beanFactory::registerBeanDefinition);
            }
        }
    }
}
