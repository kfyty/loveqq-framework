package com.kfyty.boot.configuration;

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
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private Set<String> excludeNames;

    @Getter
    private final Map<Class<?>, BeanResources> beanResources;

    private ApplicationContext() {
        this.excludeNames = new HashSet<>();
        this.beanResources = new ConcurrentHashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        return (T) Optional.ofNullable(getBeanResources(beanResources, clazz)).map(e -> e.getBean(clazz)).orElse(null);
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
            Component component = annotation.getClass().getAnnotation(Component.class);
            if (component != null) {
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
    public void registerBean(String name, Class<?> clazz, Object bean) {
        if(!this.excludeNames.contains(name)) {
            registerBean(this.beanResources, name, clazz, bean);
        }
    }

    public static ApplicationContext create() {
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.registerBean(ApplicationContext.class, applicationContext);
        return applicationContext;
    }

    public static void registerBean(Map<Class<?>, BeanResources> beanResourcesMap, Class<?> clazz, Object bean) {
        registerBean(beanResourcesMap, CommonUtil.convert2BeanName(bean.getClass().getSimpleName()), clazz, bean);
    }

    public static void registerBean(Map<Class<?>, BeanResources> beanResourcesMap, String name, Class<?> clazz, Object bean) {
        BeanResources beanResources = getBeanResources(beanResourcesMap, clazz);
        if(beanResources == null) {
            beanResourcesMap.put(clazz, new BeanResources(name, bean));
            return;
        }
        beanResources.addBean(name, bean);
    }

    private static BeanResources getBeanResources(Map<Class<?>, BeanResources> beanResourcesMap, Class<?> clazz) {
        BeanResources beanResources = beanResourcesMap.get(clazz);
        if(beanResources != null) {
            return beanResources;
        }
        for (Class<?> keyClazz : beanResourcesMap.keySet()) {
            if(clazz.isAssignableFrom(keyClazz)) {
                BeanResources br = beanResourcesMap.get(keyClazz);
                if(beanResources != null) {
                    throw new IllegalArgumentException("more than one instance of type: " + clazz.getName());
                }
                beanResources = br;
            }
        }
        return beanResources;
    }
}
