package com.kfyty.support.autoconfig;

import com.kfyty.support.autoconfig.beans.BeanDefinition;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 描述: 配置上下文
 *
 * @author kfyty725
 * @date 2021/5/21 17:44
 * @email kfyty725@hotmail.com
 */
public interface ConfigurableContext {

    Class<?> getPrimarySource();

    Map<String, BeanDefinition> getBeanDefinitions();

    BeanDefinition getBeanDefinition(String beanName);

    Map<String, BeanDefinition> getBeanDefinitions(Class<?> beanType);

    BeanDefinition getBeanDefinition(String beanName, Class<?> beanType);

    <T> T getBean(Class<T> clazz);

    <T> T getBean(String name);

    <T> Map<String, T> getBeanOfType(Class<T> clazz);

    <T> Map<String, T> getBeanWithAnnotation(Class<? extends Annotation> annotationClass);

    Object registerBean(BeanDefinition beanDefinition);

    Object registerBean(BeanDefinition beanDefinition, boolean beforeAutowired);

    void registerBean(Class<?> clazz, Object bean);

    void replaceBean(Class<?> clazz, Object bean);

    void registerBean(String name, Class<?> clazz, Object bean);

    void replaceBean(String name, Class<?> clazz, Object bean);
}
