package com.kfyty.loveqq.framework.boot.processor.factory;

import com.kfyty.loveqq.framework.boot.autoconfig.factory.ScopeProxyFactoryBean;
import com.kfyty.loveqq.framework.boot.processor.factory.internal.HardCodeBeanFactoryPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;

import java.util.HashMap;
import java.util.Map;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;
import static com.kfyty.loveqq.framework.core.utils.BeanUtil.SCOPE_PROXY_SOURCE_PREFIX;

/**
 * 描述: 作用域代理处理
 *
 * @author kfyty
 * @date 2022/10/23 15:30
 * @email kfyty725@hotmail.com
 */
@Component
public class ScopeProxyBeanFactoryPostProcessor extends HardCodeBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(BeanFactory beanFactory) {
        Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>(beanFactory.getBeanDefinitions());
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            BeanDefinition beanDefinition = beanDefinitionEntry.getValue();
            if (beanDefinition.isSingleton() || !beanDefinition.isScopeProxy() || !beanDefinition.isAutowireCandidate()) {
                continue;
            }

            BeanDefinition postProcessBeanDefinition = this.factoryBeanBeanFactoryPostProcessor.postProcessBeanDefinition(beanDefinition, beanFactory);

            beanFactory.removeBeanDefinition(postProcessBeanDefinition.getBeanName());
            beanFactory.registerBeanDefinition(genericBeanDefinition(ScopeProxyFactoryBean.class)
                    .setBeanName(beanDefinition.getBeanName())
                    .setLazyInit(beanDefinition.isLazyInit())
                    .setLazyProxy(beanDefinition.isLazyProxy())
                    .addConstructorArgs(BeanDefinition.class, postProcessBeanDefinition)
                    .getBeanDefinition());

            postProcessBeanDefinition.setAutowireCandidate(false);
            postProcessBeanDefinition.setBeanName(SCOPE_PROXY_SOURCE_PREFIX + beanDefinition.getBeanName());
            beanFactory.registerBeanDefinition(postProcessBeanDefinition, false);
        }
    }
}
