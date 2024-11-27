package com.kfyty.loveqq.framework.core.autoconfig.beans;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 描述: bean 注册
 *
 * @author kfyty725
 * @date 2021/6/14 10:59
 * @email kfyty725@hotmail.com
 */
public interface BeanDefinitionRegistry {

    void registerBeanDefinition(BeanDefinition beanDefinition);

    void registerBeanDefinition(BeanDefinition beanDefinition, boolean resolveNested);

    void registerBeanDefinition(String name, BeanDefinition beanDefinition);

    void registerBeanDefinition(String name, BeanDefinition beanDefinition, boolean resolveCondition);

    boolean containsBeanDefinition(String beanName);

    void removeBeanDefinition(String beanName);

    Collection<String> getBeanDefinitionNames(Class<?> beanType);

    BeanDefinition getBeanDefinition(String beanName);

    BeanDefinition getBeanDefinition(String beanName, Class<?> beanType);

    Map<String, BeanDefinition> getBeanDefinitions();

    Map<String, BeanDefinition> getBeanDefinitions(boolean isAutowireCandidate);

    Map<String, BeanDefinition> getBeanDefinitions(Class<?> beanType);

    Map<String, BeanDefinition> getBeanDefinitions(Class<?> beanType, boolean isAutowireCandidate);

    Map<String, BeanDefinition> getBeanDefinitionWithAnnotation(Class<? extends Annotation> annotationClass);

    Map<String, BeanDefinition> getBeanDefinitionWithAnnotation(Class<? extends Annotation> annotationClass, boolean isAutowireCandidate);

    Stream<BeanDefinition> stream(Predicate<BeanDefinition> beanDefinitionPredicate);
}
