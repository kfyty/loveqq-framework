package com.kfyty.boot.configuration;

import com.kfyty.boot.K;
import com.kfyty.boot.beans.BeanResources;
import com.kfyty.support.autoconfig.BeanDefine;
import com.kfyty.support.autoconfig.ConfigurableContext;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.util.CommonUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能描述: 应用配置
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/23 16:33
 * @since JDK 1.8
 */
@Slf4j
public class ApplicationContext implements ConfigurableContext {
    @Getter
    private final Class<?> primarySource;

    @Getter
    private final Map<Class<?>, BeanResources> beanResources;

    private ApplicationContext(Class<?> primarySource) {
        this.primarySource = primarySource;
        this.beanResources = new ConcurrentHashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        return (T) Optional.ofNullable(this.getBeanResources(clazz)).map(e -> e.getBean(clazz)).orElse(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        for (BeanResources beanResource : beanResources.values()) {
            Object bean = beanResource.getBean(name);
            if(bean != null) {
                return (T) bean;
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBeanOfType(Class<T> clazz) {
        Map<String, Object> beans = new HashMap<>(2);
        for (Class<?> keyClazz : this.beanResources.keySet()) {
            if(clazz.isAssignableFrom(keyClazz)) {
                beans.putAll(this.beanResources.get(keyClazz).getBeans());
            }
        }
        return (Map<String, T>) beans;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBeanWithAnnotation(Class<? extends Annotation> annotationClass) {
        Map<String, Object> beans = new HashMap<>(2);
        for (Class<?> clazz : beanResources.keySet()) {
            if(clazz.isAnnotationPresent(annotationClass)) {
                beans.putAll(beanResources.get(clazz).getBeans());
            }
        }
        return (Map<String, T>) beans;
    }

    @Override
    @SneakyThrows
    public Object registerBean(BeanDefine beanDefine) {
        for (Annotation annotation : beanDefine.getBeanType().getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Component.class)) {
                String beanName = (String) annotation.getClass().getMethod("value").invoke(annotation);
                if (!CommonUtil.empty(beanName)) {
                    Object instance = beanDefine.createInstance();
                    this.registerBean(beanName, beanDefine.getBeanType(), instance);
                    return instance;
                }
            }
        }
        Object instance = beanDefine.createInstance();
        this.registerBean(beanDefine.getBeanType(), instance);
        return instance;
    }

    @Override
    public void registerBean(Class<?> clazz, Object bean) {
        this.registerBean(CommonUtil.convert2BeanName(bean.getClass().getSimpleName()), clazz, bean);
    }

    @Override
    public void replaceBean(Class<?> clazz, Object bean) {
        this.replaceBean(CommonUtil.convert2BeanName(clazz.getSimpleName()), clazz, bean);
    }

    @Override
    public void registerBean(String name, Class<?> clazz, Object bean) {
        if(K.isExclude(name) || K.isExclude(clazz)) {
            log.info("exclude bean: {} -> {}", name, bean);
            return;
        }
        BeanResources beanResources = this.getBeanResources(clazz);
        if(beanResources == null) {
            this.beanResources.put(clazz, new BeanResources(name, clazz, bean));
            return;
        }
        beanResources.addBean(name, bean);
    }

    @Override
    public void replaceBean(String name, Class<?> clazz, Object bean) {
        BeanResources beanResources = this.getBeanResources(clazz);
        if(beanResources != null) {
            beanResources.getBeans().put(name, bean);
        }
    }

    public static ApplicationContext create(Class<?> primarySource) {
        ApplicationContext applicationContext = new ApplicationContext(primarySource);
        applicationContext.registerBean(ApplicationContext.class, applicationContext);
        return applicationContext;
    }

    public BeanResources getBeanResources(Class<?> clazz) {
        BeanResources beanResources = this.beanResources.get(clazz);
        if(beanResources != null) {
            return beanResources;
        }
        for (Class<?> keyClazz : this.beanResources.keySet()) {
            if(clazz.isAssignableFrom(keyClazz)) {
                BeanResources br = this.beanResources.get(keyClazz);
                if(beanResources != null) {
                    throw new IllegalArgumentException("more than one instance of type: " + clazz.getName());
                }
                beanResources = br;
            }
        }
        return beanResources;
    }
}
