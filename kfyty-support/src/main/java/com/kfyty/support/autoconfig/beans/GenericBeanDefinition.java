package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Qualifier;
import com.kfyty.support.generic.ActualGeneric;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;

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

    protected Map<Class<?>, Object> constructorArgs;

    protected AutowiredProcessor autowiredProcessor = null;

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
        if(this.constructorArgs == null) {
            this.constructorArgs = new LinkedHashMap<>(8);
        }
        this.constructorArgs.put(argType, arg);
        return this;
    }

    @Override
    public Map<Class<?>, Object> getConstructArgs() {
        return this.constructorArgs;
    }

    @Override
    public Class<?>[] getConstructArgTypes() {
        return CommonUtil.empty(getConstructArgs()) ? CommonUtil.EMPTY_CLASS_ARRAY : getConstructArgs().keySet().toArray(new Class<?>[0]);
    }

    @Override
    public Object[] getConstructArgValues() {
        return CommonUtil.empty(getConstructArgs()) ? CommonUtil.EMPTY_OBJECT_ARRAY : getConstructArgs().values().toArray();
    }

    @Override
    public Object createInstance(ApplicationContext context) {
        Object bean = context.getBean(this.getBeanName());
        if(bean != null) {
            return bean;
        }
        this.ensureAutowiredProcessor(context);
        this.prepareConstructorArgs();
        bean = ReflectUtil.newInstance(this.beanType, this.constructorArgs);
        if(log.isDebugEnabled()) {
            log.debug("instantiate bean: [{}] !", bean);
        }
        return bean;
    }

    protected void ensureAutowiredProcessor(ApplicationContext context) {
        if(this.autowiredProcessor == null) {
            this.autowiredProcessor = new AutowiredProcessor(context);
        }
    }

    protected void prepareConstructorArgs() {
        Constructor<?> constructor = ReflectUtil.searchSuitableConstructor(this.beanType, e -> AnnotationUtil.hasAnnotation(e, Autowired.class));
        if(constructor.getParameterCount() == 0 || CommonUtil.size(this.constructorArgs) == constructor.getParameterCount()) {
            return;
        }
        for (Parameter parameter : constructor.getParameters()) {
            if(this.constructorArgs != null && this.constructorArgs.containsKey(parameter.getType())) {
                continue;
            }
            String beanName = BeanUtil.getBeanName(parameter.getType(), AnnotationUtil.findAnnotation(parameter, Qualifier.class));
            Object resolveBean = this.autowiredProcessor.doResolveBean(beanName, ActualGeneric.from(parameter), AnnotationUtil.findAnnotation(parameter, Autowired.class));
            this.addConstructorArgs(parameter.getType(), resolveBean);
        }
    }

    /**
     * 从 Class 生成一个 bean 定义
     */
    public static BeanDefinition from(Class<?> beanType) {
        return new GenericBeanDefinition(beanType);
    }

    /**
     * 从 Class 生成一个 bean 定义
     * 该 bean 类型为 FactoryBean，则该 bean 的 name 由 FactoryBean#beanType$beanType 组成
     */
    public static BeanDefinition from(Class<?> factoryBeanType, Class<?> beanType) {
        String beanName = CommonUtil.format("{}${}", BeanUtil.convert2BeanName(factoryBeanType), BeanUtil.convert2BeanName(beanType));
        return from(beanName, beanType);
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
        MethodBeanDefinition beanDefinition = new MethodBeanDefinition(BeanUtil.getBeanName(beanMethod.getReturnType(), bean), beanMethod.getReturnType(), source, beanMethod);
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
}
