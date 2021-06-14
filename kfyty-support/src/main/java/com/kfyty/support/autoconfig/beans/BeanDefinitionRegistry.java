package com.kfyty.support.autoconfig.beans;

import java.util.Map;

/**
 * 描述: bean 注册
 *
 * @author kfyty725
 * @date 2021/6/14 10:59
 * @email kfyty725@hotmail.com
 */
public interface BeanDefinitionRegistry {

    void registerBeanDefinition(BeanDefinition beanDefinition);

    Map<String, BeanDefinition> getBeanDefinitions();

    Map<String, BeanDefinition> getBeanDefinitions(Class<?> beanType);

    BeanDefinition getBeanDefinition(String beanName);

    BeanDefinition getBeanDefinition(String beanName, Class<?> beanType);
}
