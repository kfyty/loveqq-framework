package com.kfyty.support.autoconfig.beans;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 描述: bean 工厂
 *
 * @author kfyty725
 * @date 2021/6/14 10:59
 * @email kfyty725@hotmail.com
 */
public interface BeanFactory extends AutoCloseable {

    boolean contains(String name);

    boolean containsReference(String name);

    <T> T getBean(Class<T> clazz);

    <T> T getBean(String name);

    <T> T getBeanReference(String name);

    <T> Map<String, T> getBeanOfType(Class<T> clazz);

    <T> Map<String, T> getBeanWithAnnotation(Class<? extends Annotation> annotationClass);

    Object registerBean(BeanDefinition beanDefinition);

    Object registerBean(Class<?> clazz, Object bean);

    Object registerBean(String name, Object bean);

    Object registerBeanReference(BeanDefinition beanDefinition);

    void replaceBean(Class<?> clazz, Object bean);

    void replaceBean(String name, Object bean);

    void removeBeanReference(String name);

    void destroyBean(String name, Object bean);
}
