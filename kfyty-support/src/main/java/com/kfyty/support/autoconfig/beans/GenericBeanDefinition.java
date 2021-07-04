package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.generic.ActualGeneric;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 描述: 简单的通用 bean 定义
 *
 * @author kfyty725
 * @date 2021/6/12 10:29
 * @email kfyty725@hotmail.com
 */
@Slf4j
@ToString
@EqualsAndHashCode
public class GenericBeanDefinition implements BeanDefinition {
    /**
     * 该 bean 注册的名称
     */
    protected final String beanName;

    /**
     * 该 bean 注册的类型
     */
    protected final Class<?> beanType;

    protected Constructor<?> constructor;

    protected Map<Class<?>, Object> defaultConstructorArgs;

    protected static AutowiredProcessor autowiredProcessor = null;

    public GenericBeanDefinition(Class<?> beanType) {
        this(BeanUtil.convert2BeanName(beanType), beanType);
    }

    public GenericBeanDefinition(String beanName, Class<?> beanType) {
        this.beanName = beanName;
        this.beanType = beanType;
    }

    @Override
    public String getBeanName() {
        return this.beanName;
    }

    @Override
    public Class<?> getBeanType() {
        return this.beanType;
    }

    @Override
    public BeanDefinition addConstructorArgs(Class<?> argType, Object arg) {
        if(this.defaultConstructorArgs == null) {
            this.defaultConstructorArgs = new LinkedHashMap<>(4);
        }
        this.defaultConstructorArgs.put(argType, arg);
        return this;
    }

    @Override
    public Map<Class<?>, Object> getConstructArgs() {
        return this.prepareConstructorArgs();
    }

    @Override
    public Class<?>[] getConstructArgTypes() {
        this.ensureConstructor();
        return this.constructor.getParameterTypes();
    }

    @Override
    public Object[] getConstructArgValues() {
        return this.getConstructArgs().values().toArray();
    }

    @Override
    public Object createInstance(ApplicationContext context) {
        if(context.contains(this.getBeanName())) {
            return context.getBean(this.getBeanName());
        }
        this.ensureAutowiredProcessor(context);
        Object bean = ReflectUtil.newInstance(this.beanType, this.getConstructArgs());
        if(log.isDebugEnabled()) {
            log.debug("instantiate bean: [{}] !", bean);
        }
        return bean;
    }

    protected void ensureConstructor() {
        if(this.constructor == null) {
            this.constructor = ReflectUtil.searchSuitableConstructor(this.beanType, e -> AnnotationUtil.hasAnnotation(e, Autowired.class));
        }
    }

    protected void ensureAutowiredProcessor(ApplicationContext context) {
        if(autowiredProcessor == null || autowiredProcessor.getContext() != context) {
            autowiredProcessor = new AutowiredProcessor(context);
        }
    }

    protected Map<Class<?>, Object> prepareConstructorArgs() {
        this.ensureConstructor();
        if(this.constructor.getParameterCount() == 0) {
            return Collections.emptyMap();
        }
        Parameter[] parameters = this.constructor.getParameters();
        Map<Class<?>, Object> constructorArgs = Optional.ofNullable(this.defaultConstructorArgs).map(LinkedHashMap::new).orElse(new LinkedHashMap<>(4));
        for (int i = constructorArgs.size(); i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Autowired autowired = AnnotationUtil.findAnnotation(parameter, Autowired.class);
            Object resolveBean = autowiredProcessor.doResolveBean(BeanUtil.getBeanName(parameter), ActualGeneric.from(parameter), autowired != null ? autowired : AnnotationUtil.findAnnotation(this.constructor, Autowired.class));
            constructorArgs.put(parameter.getType(), resolveBean);
        }
        return constructorArgs;
    }

    /**
     * 从 Class 生成一个 bean 定义
     */
    public static BeanDefinition from(Class<?> beanType) {
        return new GenericBeanDefinition(findBeanName(beanType), beanType);
    }

    /**
     * 从 Class 生成一个 bean 定义
     * 该 bean 类型为 FactoryBean，该 bean 的 name 由 targetBeanTypeBeanName$factoryBeanTypeBeanName 组成
     */
    public static BeanDefinition from(Class<?> targetBeanType, Class<?> factoryBeanType) {
        String beanName = CommonUtil.format("{}${}", BeanUtil.convert2BeanName(targetBeanType), BeanUtil.convert2BeanName(factoryBeanType));
        return from(beanName, factoryBeanType);
    }

    /**
     * 从 Class 生成一个 bean 定义
     */
    public static BeanDefinition from(String beanName, Class<?> beanType) {
        return new GenericBeanDefinition(beanName, beanType);
    }

    /**
     * 从 Bean 注解的方法生成一个 Bean 定义
     */
    public static BeanDefinition from(BeanDefinition source, Method beanMethod, Bean bean) {
        MethodBeanDefinition beanDefinition = new MethodBeanDefinition(BeanUtil.getBeanName(beanMethod, bean), beanMethod.getReturnType(), source, beanMethod);
        if(CommonUtil.notEmpty(bean.initMethod())) {
            beanDefinition.setInitMethodName(bean.initMethod());
        }
        if(CommonUtil.notEmpty(bean.destroyMethod())) {
            beanDefinition.setDestroyMethodName(bean.destroyMethod());
        }
        return beanDefinition;
    }

    /**
     * 从 FactoryBean 的 bean 定义生成一个 bean 定义
     */
    public static BeanDefinition from(BeanDefinition factoryBeanDefinition) {
        return new FactoryBeanDefinition(factoryBeanDefinition);
    }

    /**
     * 从 bean type 解析 bean name
     */
    public static String findBeanName(Class<?> beanType) {
        Component component = AnnotationUtil.findAnnotation(beanType, Component.class);
        if(component != null && CommonUtil.notEmpty(component.value())) {
            return component.value();
        }
        for (Annotation annotation : AnnotationUtil.findAnnotations(beanType)) {
            if (AnnotationUtil.hasAnnotation(annotation.annotationType(), Component.class)) {
                String beanName = ReflectUtil.invokeSimpleMethod(annotation, "value");
                if (CommonUtil.notEmpty(beanName)) {
                    return beanName;
                }
            }
        }
        return BeanUtil.convert2BeanName(beanType);
    }
}
