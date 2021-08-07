package com.kfyty.support.autoconfig.beans.builder;

import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.EnableAutoConfiguration;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.FactoryBean;
import com.kfyty.support.autoconfig.beans.FactoryBeanDefinition;
import com.kfyty.support.autoconfig.beans.GenericBeanDefinition;
import com.kfyty.support.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.support.io.FactoriesLoader;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import com.kfyty.support.utils.ScopeUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 描述: 创建 bean 定义
 *
 * @author kfyty725
 * @date 2021/8/6 22:09
 * @email kfyty725@hotmail.com
 */
public class BeanDefinitionBuilder {
    private final GenericBeanDefinition beanDefinition;
    private final Map<Class<?>, Object> defaultConstructorArgs;

    private BeanDefinitionBuilder(GenericBeanDefinition beanDefinition) {
        this.beanDefinition = beanDefinition;
        this.defaultConstructorArgs = new LinkedHashMap<>();
    }

    public BeanDefinitionBuilder setBeanName(String beanName) {
        this.beanDefinition.setBeanName(beanName);
        return this;
    }

    public BeanDefinitionBuilder setBeanType(Class<?> beanType) {
        this.beanDefinition.setBeanType(beanType);
        return this;
    }

    public BeanDefinitionBuilder setScope(String scope) {
        this.beanDefinition.setScope(scope);
        return this;
    }

    public BeanDefinitionBuilder setAutowireCandidate(boolean autowireCandidate) {
        this.beanDefinition.setAutowireCandidate(autowireCandidate);
        return this;
    }

    public BeanDefinitionBuilder addConstructorArgs(Class<?> argType, Object arg) {
        this.defaultConstructorArgs.put(argType, arg);
        return this;
    }

    public BeanDefinition getBeanDefinition() {
        this.validate();
        if (FactoryBean.class.isAssignableFrom(this.beanDefinition.getBeanType())) {
            FactoryBean<?> factoryBean = (FactoryBean<?>) ReflectUtil.newInstance(this.beanDefinition.getBeanType(), this.defaultConstructorArgs);
            String factoryBeanName = this.beanDefinition.getBeanType().getName() + "$$FactoryBean$$" + factoryBean.getBeanName();
            this.beanDefinition.setBeanName(factoryBeanName);
            FactoryBeanDefinition.addSnapFactoryBeanCache(factoryBeanName, factoryBean);
        }
        return this.beanDefinition;
    }

    public void validate() {
        if (CommonUtil.empty(this.beanDefinition.getBeanName())) {
            throw new IllegalStateException("bean name can't empty !");
        }
        if (this.beanDefinition.getBeanType() == null) {
            throw new IllegalStateException("bean type can't empty !");
        }
        if (CommonUtil.empty(this.beanDefinition.getScope())) {
            this.beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        }
        if (CommonUtil.notEmpty(this.defaultConstructorArgs)) {
            this.defaultConstructorArgs.forEach(this.beanDefinition::addConstructorArgs);
        }
    }

    public static BeanDefinitionBuilder genericBeanDefinition() {
        return new BeanDefinitionBuilder(new GenericBeanDefinition());
    }

    public static BeanDefinitionBuilder genericBeanDefinition(Class<?> beanType) {
        return genericBeanDefinition(resolveBeanName(beanType), beanType);
    }

    public static BeanDefinitionBuilder genericBeanDefinition(String beanName, Class<?> beanType) {
        return new BeanDefinitionBuilder(new GenericBeanDefinition(beanName, beanType, ScopeUtil.resolveScope(beanType).value()));
    }

    public static BeanDefinitionBuilder genericBeanDefinition(BeanDefinition source, Method beanMethod, Bean bean) {
        MethodBeanDefinition beanDefinition = new MethodBeanDefinition(BeanUtil.getBeanName(beanMethod, bean), beanMethod.getReturnType(), source, beanMethod);
        if (CommonUtil.notEmpty(bean.initMethod())) {
            beanDefinition.setInitMethodName(bean.initMethod());
        }
        if (CommonUtil.notEmpty(bean.destroyMethod())) {
            beanDefinition.setDestroyMethodName(bean.destroyMethod());
        }
        return new BeanDefinitionBuilder(beanDefinition);
    }

    public static BeanDefinitionBuilder factoryBeanDefinition(BeanDefinition factoryBeanDefinition) {
        return new BeanDefinitionBuilder(new FactoryBeanDefinition(factoryBeanDefinition));
    }

    public static String resolveBeanName(Class<?> beanType) {
        Component component = AnnotationUtil.findAnnotation(beanType, Component.class);
        if (component != null && CommonUtil.notEmpty(component.value())) {
            return component.value();
        }
        for (Annotation annotation : AnnotationUtil.findAnnotations(beanType)) {
            if (AnnotationUtil.hasAnnotationElement(annotation.annotationType(), Component.class)) {
                String beanName = ReflectUtil.invokeSimpleMethod(annotation, "value");
                if (CommonUtil.notEmpty(beanName)) {
                    return beanName;
                }
            }
        }
        if (FactoriesLoader.loadFactories(EnableAutoConfiguration.class).contains(beanType.getName())) {
            return beanType.getName();
        }
        return BeanUtil.convert2BeanName(beanType);
    }
}
