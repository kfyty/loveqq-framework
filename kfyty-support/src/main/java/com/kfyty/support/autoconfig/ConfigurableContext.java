package com.kfyty.support.autoconfig;

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

    <T> T getBean(Class<T> clazz);

    <T> T getBean(String name);

    <T> Map<String, T> getBeanOfType(Class<T> clazz);

    <T> Map<String, T> getBeanWithAnnotation(Class<? extends Annotation> annotationClass);

    Object registerBean(BeanDefine beanDefine);

    void registerBean(Class<?> clazz, Object bean);

    void replaceBean(Class<?> clazz, Object bean);

    void registerBean(String name, Class<?> clazz, Object bean);

    void replaceBean(String name, Class<?> clazz, Object bean);
}
