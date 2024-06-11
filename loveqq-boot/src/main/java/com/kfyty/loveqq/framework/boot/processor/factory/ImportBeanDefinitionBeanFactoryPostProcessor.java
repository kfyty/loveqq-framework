package com.kfyty.loveqq.framework.boot.processor.factory;

import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ImportBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;

import java.util.Map;

/**
 * 描述: 导入自定义 bean 定义
 * <p>
 * {@link ImportBeanDefinitionBeanFactoryPostProcessor}
 * <p>
 * ... 其他
 * <p>
 * {@link ScopeProxyBeanFactoryPostProcessor}
 * {@link LazyProxyBeanFactoryPostProcessor}
 * {@link FactoryBeanBeanFactoryPostProcessor}
 *
 * @author kfyty
 * @date 2022/10/23 15:30
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Integer.MIN_VALUE)
public class ImportBeanDefinitionBeanFactoryPostProcessor implements BeanFactoryPostProcessor, InternalPriority {

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
