package com.kfyty.support.autoconfig.beans;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 描述: bean 工厂
 *
 * @author kfyty725
 * @date 2021/6/14 10:59
 * @email kfyty725@hotmail.com
 */
public interface BeanFactory {

    <T> T getBean(Class<T> clazz);

    <T> T getBean(String name);

    <T> Map<String, T> getBeanOfType(Class<T> clazz);

    <T> Map<String, T> getBeanWithAnnotation(Class<? extends Annotation> annotationClass);

    Object registerBean(BeanDefinition beanDefinition);

    Object registerBean(BeanDefinition beanDefinition, boolean beforeAutowired);

    void registerBean(Class<?> clazz, Object bean);

    void registerBean(String name, Object bean);

    void replaceBean(Class<?> clazz, Object bean);

    void replaceBean(String name, Object bean);

    void forEach(BiConsumer<String, Object> bean);
}
