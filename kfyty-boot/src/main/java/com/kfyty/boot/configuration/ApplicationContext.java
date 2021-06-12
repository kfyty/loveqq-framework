package com.kfyty.boot.configuration;

import com.kfyty.boot.K;
import com.kfyty.boot.beans.BeanResources;
import com.kfyty.boot.resolver.AnnotationConfigResolver;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.ConfigurableContext;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.FactoryBeanDefinition;
import com.kfyty.support.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.support.utils.BeanUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    private final AnnotationConfigResolver configResolver;

    @Getter
    private final Map<Class<?>, BeanResources> beanResources;

    public ApplicationContext(Class<?> primarySource, AnnotationConfigResolver configResolver) {
        this.primarySource = primarySource;
        this.configResolver = configResolver;
        this.beanResources = new ConcurrentHashMap<>();
        this.registerBean(ApplicationContext.class, this);
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitions() {
        return this.configResolver.getBeanDefinitions();
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return this.getBeanDefinitions().get(beanName);
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitions(Class<?> beanType) {
        return getBeanDefinitions().entrySet().stream().filter(e -> beanType.isAssignableFrom(e.getValue().getBeanType())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName, Class<?> beanType) {
        Map<String, BeanDefinition> beanDefinitions = this.getBeanDefinitions(beanType);
        return beanDefinitions.size() == 1 ? beanDefinitions.values().iterator().next() : beanDefinitions.get(beanName);
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
    public Object registerBean(BeanDefinition beanDefinition) {
        return this.registerBean(beanDefinition, true);
    }

    @Override
    public Object registerBean(BeanDefinition beanDefinition, boolean beforeAutowired) {
        Object bean = this.getBean(beanDefinition.getBeanName());
        if(bean != null) {
            return bean;
        }
        if(beforeAutowired && beanDefinition instanceof MethodBeanDefinition) {
            BeanDefinition sourceDefinition = ((MethodBeanDefinition) beanDefinition).getSourceDefinition();
            this.configResolver.getFieldAnnotationResolver().doResolver(sourceDefinition.getBeanType(), registerBean(sourceDefinition), true);
        }
        if(beforeAutowired && beanDefinition instanceof FactoryBeanDefinition) {
            BeanDefinition factoryBeanDefinition = ((FactoryBeanDefinition) beanDefinition).getFactoryBeanDefinition();
            this.configResolver.getFieldAnnotationResolver().doResolver(factoryBeanDefinition.getBeanType(), registerBean(factoryBeanDefinition), true);
        }
        bean = this.getBean(beanDefinition.getBeanName());
        if(bean != null) {
            return bean;
        }
        bean = beanDefinition.createInstance(this);
        if(bean instanceof ApplicationContextAware) {
            ((ApplicationContextAware) bean).setApplicationContext(this);
        }
        this.registerBean(beanDefinition.getBeanName(), beanDefinition.getBeanType(), bean);
        return bean;
    }

    @Override
    public void registerBean(Class<?> clazz, Object bean) {
        this.registerBean(BeanUtil.convert2BeanName(clazz), clazz, bean);
    }

    @Override
    public void replaceBean(Class<?> clazz, Object bean) {
        this.replaceBean(BeanUtil.convert2BeanName(clazz), clazz, bean);
    }

    @Override
    public void registerBean(String name, Class<?> clazz, Object bean) {
        if(K.isExclude(name) || K.isExclude(clazz)) {
            log.info("exclude bean: {} -> {}", name, bean);
            return;
        }
        synchronized (this) {
            BeanResources beanResources = this.getBeanResources(clazz);
            if(beanResources == null) {
                this.beanResources.put(clazz, new BeanResources(name, clazz, bean));
                return;
            }
            beanResources.addBean(name, bean);
        }
    }

    @Override
    public void replaceBean(String name, Class<?> clazz, Object bean) {
        BeanResources beanResources = this.getBeanResources(clazz);
        if(beanResources != null) {
            beanResources.getBeans().put(name, bean);
        }
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
