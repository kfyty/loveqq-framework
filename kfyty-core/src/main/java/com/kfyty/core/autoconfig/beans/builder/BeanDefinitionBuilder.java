package com.kfyty.core.autoconfig.beans.builder;

import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.EnableAutoConfiguration;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.FactoryBean;
import com.kfyty.core.autoconfig.beans.FactoryBeanDefinition;
import com.kfyty.core.autoconfig.beans.GenericBeanDefinition;
import com.kfyty.core.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.core.autoconfig.beans.autowired.AutowiredDescription;
import com.kfyty.core.autoconfig.beans.autowired.property.PropertyValue;
import com.kfyty.core.io.FactoriesLoader;
import com.kfyty.core.support.Pair;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.BeanUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.core.utils.ScopeUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import static com.kfyty.core.autoconfig.beans.FactoryBeanDefinition.FACTORY_BEAN_PREFIX;
import static com.kfyty.core.autoconfig.beans.FactoryBeanDefinition.addSnapFactoryBeanCache;
import static com.kfyty.core.utils.ReflectUtil.newInstance;

/**
 * 描述: 创建 bean 定义
 *
 * @author kfyty725
 * @date 2021/8/6 22:09
 * @email kfyty725@hotmail.com
 */
public class BeanDefinitionBuilder {
    private final GenericBeanDefinition beanDefinition;
    private final List<Pair<Class<?>, Object>> defaultConstructorArgs;

    private BeanDefinitionBuilder(GenericBeanDefinition beanDefinition) {
        this.beanDefinition = beanDefinition;
        this.defaultConstructorArgs = new LinkedList<>();
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

    public BeanDefinitionBuilder setScopeProxy(boolean isScopeProxy) {
        this.beanDefinition.setScopeProxy(isScopeProxy);
        return this;
    }

    public BeanDefinitionBuilder setLazyInit(boolean isLazyInit) {
        this.beanDefinition.setLazyInit(isLazyInit);
        return this;
    }

    public BeanDefinitionBuilder setLazyProxy(boolean isLazyProxy) {
        this.beanDefinition.setLazyProxy(isLazyProxy);
        return this;
    }

    public BeanDefinitionBuilder setAutowireCandidate(boolean autowireCandidate) {
        this.beanDefinition.setAutowireCandidate(autowireCandidate);
        return this;
    }

    public BeanDefinitionBuilder addConstructorArgs(Class<?> argType, Object arg) {
        this.defaultConstructorArgs.add(new Pair<>(argType, arg));
        return this;
    }

    public BeanDefinitionBuilder addPropertyValue(String name, Object value) {
        return this.addPropertyValue(new PropertyValue().setName(name).setValue(value).setPropertyType(PropertyValue.PropertyType.VALUE));
    }

    public BeanDefinitionBuilder addPropertyValue(String name, Class<?> requiredType) {
        return this.addPropertyValue(name, null, requiredType);
    }

    public BeanDefinitionBuilder addPropertyValue(String name, String reference, Class<?> requiredType) {
        PropertyValue propertyValue = new PropertyValue()
                .setName(name)
                .setReferenceType(requiredType)
                .setReference(new AutowiredDescription(reference, true))
                .setPropertyType(PropertyValue.PropertyType.REFERENCE);
        return this.addPropertyValue(propertyValue);
    }

    public BeanDefinitionBuilder addPropertyValue(PropertyValue propertyValue) {
        this.beanDefinition.addPropertyValue(propertyValue);
        return this;
    }

    public BeanDefinition getBeanDefinition() {
        this.validate();
        if (this.beanDefinition instanceof FactoryBeanDefinition) {
            return this.beanDefinition;
        }
        if (FactoryBean.class.isAssignableFrom(this.beanDefinition.getBeanType())) {
            this.beanDefinition.setBeanName(FACTORY_BEAN_PREFIX + this.beanDefinition.getBeanName());
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
        if (!this.beanDefinition.isLazyInit()) {
            this.setLazyInit(BeanUtil.isLazyInit(this.beanDefinition));
        }
        if (CommonUtil.empty(this.beanDefinition.getScope())) {
            this.setScope(BeanDefinition.SCOPE_SINGLETON);
        }
        if (CommonUtil.notEmpty(this.defaultConstructorArgs)) {
            this.defaultConstructorArgs.forEach(e -> this.beanDefinition.addConstructorArgs(e.getKey(), e.getValue()));
        }
        if (FactoryBean.class.isAssignableFrom(this.beanDefinition.getBeanType())) {
            if (!(this.beanDefinition instanceof MethodBeanDefinition) && this.beanDefinition.getBeanName().equals(resolveBeanName(this.beanDefinition.getBeanType()))) {
                FactoryBean<?> factoryBean = (FactoryBean<?>) newInstance(this.beanDefinition.getBeanType(), this.defaultConstructorArgs);
                this.setBeanName(resolveBeanName(factoryBean.getBeanType()));
                addSnapFactoryBeanCache(FACTORY_BEAN_PREFIX + this.beanDefinition.getBeanName(), factoryBean);
            }
        }
    }

    public static BeanDefinitionBuilder genericBeanDefinition() {
        return new BeanDefinitionBuilder(new GenericBeanDefinition());
    }

    public static BeanDefinitionBuilder genericBeanDefinition(Class<?> beanType) {
        return genericBeanDefinition(resolveBeanName(beanType), beanType);
    }

    public static BeanDefinitionBuilder genericBeanDefinition(String beanName, Class<?> beanType) {
        return new BeanDefinitionBuilder(new GenericBeanDefinition(beanName, beanType, ScopeUtil.resolveScope(beanType)));
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
                String beanName = ReflectUtil.invokeMethod(annotation, "value");
                if (CommonUtil.notEmpty(beanName)) {
                    return beanName;
                }
            }
        }
        if (FactoriesLoader.loadFactories(EnableAutoConfiguration.class).contains(beanType.getName())) {
            return beanType.getName();
        }
        return BeanUtil.getBeanName(beanType);
    }
}
