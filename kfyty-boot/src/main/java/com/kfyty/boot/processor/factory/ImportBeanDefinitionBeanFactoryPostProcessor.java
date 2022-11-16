package com.kfyty.boot.processor.factory;

import com.kfyty.core.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.core.autoconfig.ImportBeanDefinition;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.BeanFactory;

import java.util.Map;

/**
 * 描述: 导入自定义 bean 定义
 *
 * @author kfyty
 * @date 2022/10/23 15:30
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Integer.MIN_VALUE)
public class ImportBeanDefinitionBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(BeanFactory beanFactory) {
        if (beanFactory instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) beanFactory;
            Map<String, BeanDefinition> importBeanDefines = beanFactory.getBeanDefinitions(ImportBeanDefinition.class);
            for (BeanDefinition importBeanDefine : importBeanDefines.values()) {
                ImportBeanDefinition bean = (ImportBeanDefinition) beanFactory.registerBean(importBeanDefine);
                for (BeanDefinition beanDefinition : bean.doImport(applicationContext, applicationContext.getScannedClasses())) {
                    beanFactory.registerBeanDefinition(beanDefinition);
                }
            }
        }
    }
}
