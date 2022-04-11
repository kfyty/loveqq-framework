package com.kfyty.support.autoconfig.beans;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 描述: bean 注册
 *
 * @author kfyty725
 * @date 2021/6/14 10:59
 * @email kfyty725@hotmail.com
 */
public interface BeanDefinitionRegistry {

    void registerBeanDefinition(BeanDefinition beanDefinition);

    boolean containsBeanDefinition(String beanName);

    void removeBeanDefinition(String beanName);

    Map<String, BeanDefinition> getBeanDefinitions();

    Map<String, BeanDefinition> getBeanDefinitions(Class<?> beanType);

    Map<String, BeanDefinition> getBeanDefinitionWithAnnotation(Class<? extends Annotation> annotationClass);

    Map<String, BeanDefinition> getBeanDefinitions(Predicate<Map.Entry<String, BeanDefinition>> beanDefinitionPredicate);

    BeanDefinition getBeanDefinition(String beanName);

    BeanDefinition getBeanDefinition(String beanName, Class<?> beanType);
}
