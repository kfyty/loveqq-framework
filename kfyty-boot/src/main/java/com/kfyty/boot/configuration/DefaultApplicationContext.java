package com.kfyty.boot.configuration;

import com.kfyty.boot.resolver.AnnotationConfigResolver;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.FactoryBeanDefinition;
import com.kfyty.support.autoconfig.beans.InstantiatedBeanDefinition;
import com.kfyty.support.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.support.exception.BeansException;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * 功能描述: 应用配置
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/23 16:33
 * @since JDK 1.8
 */
@Slf4j
public class DefaultApplicationContext implements ApplicationContext {
    @Getter
    private final AnnotationConfigResolver configResolver;

    private final Map<String, BeanDefinition> beanDefinitions;

    private final Map<String, Object> beanInstances;

    public DefaultApplicationContext(AnnotationConfigResolver configResolver) {
        this.configResolver = configResolver;
        this.beanDefinitions = new LinkedHashMap<>();
        this.beanInstances = new ConcurrentHashMap<>();
    }

    @Override
    public Class<?> getPrimarySource() {
        return this.configResolver.getPrimarySource();
    }

    @Override
    public void registerBeanDefinition(BeanDefinition beanDefinition) {
        if(this.beanDefinitions.containsKey(beanDefinition.getBeanName())) {
            throw new BeansException("conflicting bean definition: " + beanDefinition.getBeanName());
        }
        this.beanDefinitions.putIfAbsent(beanDefinition.getBeanName(), beanDefinition);
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitions() {
        return this.beanDefinitions;
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
    public <T> T getBean(Class<T> clazz) {
        Map<String, T> beanOfType = this.getBeanOfType(clazz);
        if(beanOfType.size() > 1) {
            throw new BeansException("more than one instance of type: " + clazz.getName());
        }
        return beanOfType.isEmpty() ? null : beanOfType.values().iterator().next();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        return (T) this.beanInstances.get(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBeanOfType(Class<T> clazz) {
        Map<String, Object> beans = new HashMap<>(2);
        for (BeanDefinition beanDefinition : this.getBeanDefinitions().values()) {
            if(clazz.isAssignableFrom(beanDefinition.getBeanType())) {
                beans.put(beanDefinition.getBeanName(), this.registerBean(beanDefinition));
            }
        }
        return (Map<String, T>) beans;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBeanWithAnnotation(Class<? extends Annotation> annotationClass) {
        Map<String, Object> beans = new HashMap<>(2);
        for (BeanDefinition beanDefinition : this.getBeanDefinitions().values()) {
            if(AnnotationUtil.hasAnnotation(beanDefinition.getBeanType(), annotationClass)) {
                beans.put(beanDefinition.getBeanName(), this.registerBean(beanDefinition));
            }
        }
        return (Map<String, T>) beans;
    }

    @Override
    public Object registerBean(BeanDefinition beanDefinition) {
        return this.registerBean(beanDefinition, true);
    }

    /**
     * 根据 BeanDefinition 注册一个 bean
     * 如果该 BeanDefinition 是由 Bean 注解生成的，则注册前会尝试先注入属性
     * 由于注入属性可能会级联注册该 BeanDefinition，因此需要做二次判断
     * 由于 Configuration 注解的 bean 被代理，因此该 BeanDefinition 可能在代理中已被注册，所以注册时需做三次判断
     * @param beanDefinition BeanDefinition
     * @param beforeAutowired 注册前是否先注入属性
     * @return bean
     */
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
        if(this.getBean(beanDefinition.getBeanName()) == null) {
            this.registerBean(beanDefinition.getBeanName(), bean);
        }
        return bean;
    }

    @Override
    public void registerBean(Class<?> clazz, Object bean) {
        this.registerBean(BeanUtil.convert2BeanName(clazz), bean);
    }

    @Override
    public void registerBean(String name, Object bean) {
        synchronized (this) {
            if(this.beanInstances.containsKey(name)) {
                throw new BeansException("conflicting bean name: " + name);
            }
            this.beanInstances.put(name, bean);
            if(!this.beanDefinitions.containsKey(name)) {
                this.registerBeanDefinition(InstantiatedBeanDefinition.from(name, bean.getClass()));
            }
            if(bean instanceof ApplicationContextAware) {
                ((ApplicationContextAware) bean).setApplicationContext(this);
            }
            this.configResolver.doBeanPostProcessAfterInstantiation(name, bean);
        }
    }

    @Override
    public void replaceBean(Class<?> clazz, Object bean) {
        this.replaceBean(BeanUtil.convert2BeanName(clazz), bean);
    }

    @Override
    public void replaceBean(String name, Object bean) {
        if(bean != null) {
            this.beanInstances.put(name, bean);
        }
    }

    public void doInBeans(BiConsumer<String, Object> bean) {
        for (Map.Entry<String, Object> entry : this.beanInstances.entrySet()) {
            bean.accept(entry.getKey(), entry.getValue());
        }
    }
}
