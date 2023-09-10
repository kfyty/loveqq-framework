package com.kfyty.core.autoconfig.beans;

import com.kfyty.core.autoconfig.BeanPostProcessor;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 描述: bean 工厂
 *
 * @author kfyty725
 * @date 2021/6/14 10:59
 * @email kfyty725@hotmail.com
 */
public interface BeanFactory extends ConditionBeanDefinitionRegistry, AutoCloseable {

    boolean contains(String name);

    <T> T getBean(Class<T> clazz);

    <T> T getBean(Class<T> clazz, boolean isLazyInit);

    <T> T getBean(String name);

    <T> T getBean(String name, boolean isLazyInit);

    <T> Map<String, T> getBeanOfType(Class<T> clazz);

    <T> Map<String, T> getBeanWithAnnotation(Class<? extends Annotation> annotationClass);

    void registerBeanPostProcessors(String beanName, BeanPostProcessor beanPostProcessor);

    Object registerBean(BeanDefinition beanDefinition);

    Object registerBean(BeanDefinition beanDefinition, boolean isLazyInit);

    Object registerBean(Class<?> clazz, Object bean);

    Object registerBean(String name, Object bean);

    void replaceBean(String name, Object bean);

    boolean containsReference(String name);

    void registerBeanReference(BeanDefinition beanDefinition);

    void removeBeanReference(String name);

    void destroyBean(String name, Object bean);
}
