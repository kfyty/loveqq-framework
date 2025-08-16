package com.kfyty.loveqq.framework.boot.processor.factory;

import com.kfyty.loveqq.framework.boot.processor.factory.internal.HardCodeBeanFactoryPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * 描述: {@link FactoryBean} 处理
 *
 * @author kfyty725
 * @date 2022/10/23 15:30
 * @email kfyty725@hotmail.com
 */
@Component
public class FactoryBeanBeanFactoryPostProcessor extends HardCodeBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    /**
     * 已经处理的 bean name
     */
    protected volatile Set<String> postProcessed;

    @Override
    public void postProcessBeanFactory(BeanFactory beanFactory) {
        Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>(beanFactory.getBeanDefinitions());
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            this.postProcessBeanDefinition(beanDefinitionEntry.getValue(), beanFactory);
        }
    }

    public BeanDefinition postProcessBeanDefinition(BeanDefinition beanDefinition, BeanFactory beanFactory) {
        if (beanDefinition.isAutowireCandidate() && beanDefinition.isFactoryBean()) {
            if (!this.addPostProcessed(beanDefinition.getBeanName())) {
                BeanDefinition factoryBeanDefinition = genericBeanDefinition(beanDefinition).getBeanDefinition();
                beanFactory.registerBeanDefinition(factoryBeanDefinition, false);
                return factoryBeanDefinition;
            }
        }
        return beanDefinition;
    }

    protected boolean addPostProcessed(String beanName) {
        if (this.postProcessed == null) {
            synchronized (this) {
                if (this.postProcessed == null) {
                    this.postProcessed = Collections.synchronizedSet(new HashSet<>());
                }
            }
        }
        return !this.postProcessed.add(beanName);
    }
}
