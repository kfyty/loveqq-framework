package com.kfyty.core.autoconfig.beans;

import com.kfyty.core.autoconfig.condition.ConditionContext;

import java.util.Map;

/**
 * 描述: 条件 bean 注册
 *
 * @author kfyty725
 * @date 2022/10/29 16:15
 * @email kfyty725@hotmail.com
 */
public interface ConditionBeanDefinitionRegistry extends BeanDefinitionRegistry {

    ConditionContext getConditionContext();

    void registerConditionBeanDefinition(BeanDefinition beanDefinition);

    void resolveNestedBeanDefinitionReference(BeanDefinition beanDefinition);

    void resolveRegisterNestedBeanDefinition(BeanDefinition beanDefinition);

    void registerConditionBeanDefinition(String name, BeanDefinition beanDefinition);

    void registerConditionBeanDefinition(String name, ConditionalBeanDefinition conditionalBeanDefinition);

    Map<String, ConditionalBeanDefinition> getConditionalBeanDefinition();

    void resolveConditionBeanDefinitionRegistry();
}
