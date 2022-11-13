package com.kfyty.boot.processor.factory;

import com.kfyty.boot.autoconfig.factory.ScopeProxyFactoryBean;
import com.kfyty.core.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.utils.ScopeUtil;

import java.util.HashMap;
import java.util.Map;

import static com.kfyty.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;
import static com.kfyty.core.utils.BeanUtil.SCOPE_PROXY_SOURCE_PREFIX;

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

            if (!ScopeUtil.resolveScope(beanDefinition).scopeProxy()) {
                continue;
            }

            BeanDefinition postProcessBeanDefinition = this.factoryBeanBeanFactoryPostProcessor.postProcessBeanDefinition(beanDefinition, beanFactory);

            beanFactory.removeBeanDefinition(postProcessBeanDefinition.getBeanName());
            beanFactory.registerBeanDefinition(genericBeanDefinition(ScopeProxyFactoryBean.class).setBeanName(beanDefinition.getBeanName()).setLazyInit(beanDefinition.isLazyInit()).addConstructorArgs(BeanDefinition.class, postProcessBeanDefinition).getBeanDefinition());

            postProcessBeanDefinition.setAutowireCandidate(false);
            postProcessBeanDefinition.setBeanName(SCOPE_PROXY_SOURCE_PREFIX + beanDefinition.getBeanName());
            beanFactory.registerBeanDefinition(postProcessBeanDefinition);
        }
    }
}
