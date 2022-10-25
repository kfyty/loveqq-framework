package com.kfyty.boot.processor.factory;

import com.kfyty.boot.autoconfig.factory.ScopeProxyFactoryBean;
import com.kfyty.support.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.BeanFactory;

import java.util.HashMap;
import java.util.Map;

import static com.kfyty.support.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;
import static com.kfyty.support.utils.BeanUtil.SCOPE_PROXY_SOURCE_PREFIX;

/**
 * 描述:
 *
 * @author kfyty
 * @date 2022/10/23 15:30
 * @email kfyty725@hotmail.com
 */
@Component
public class ScopeProxyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Autowired
    protected FactoryBeanBeanFactoryPostProcessor factoryBeanBeanFactoryPostProcessor;

    @Override
    public void postProcessBeanFactory(BeanFactory beanFactory) {
        Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>(beanFactory.getBeanDefinitions());
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            BeanDefinition beanDefinition = beanDefinitionEntry.getValue();
            if (beanDefinition.isSingleton() || !beanDefinition.isAutowireCandidate()) {
                continue;
            }

            BeanDefinition postProcessBeanDefinition = this.factoryBeanBeanFactoryPostProcessor.postProcessBeanDefinition(beanDefinition, beanFactory);

            beanFactory.removeBeanDefinition(postProcessBeanDefinition.getBeanName());
            beanFactory.registerBeanDefinition(genericBeanDefinition(ScopeProxyFactoryBean.class).setBeanName(beanDefinition.getBeanName()).addConstructorArgs(BeanDefinition.class, postProcessBeanDefinition).getBeanDefinition());

            postProcessBeanDefinition.setAutowireCandidate(false);
            postProcessBeanDefinition.setBeanName(SCOPE_PROXY_SOURCE_PREFIX + beanDefinition.getBeanName());
            beanFactory.registerBeanDefinition(postProcessBeanDefinition);
        }
    }
}
