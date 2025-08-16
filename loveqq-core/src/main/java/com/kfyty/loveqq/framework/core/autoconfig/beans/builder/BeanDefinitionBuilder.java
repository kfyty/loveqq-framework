package com.kfyty.loveqq.framework.core.autoconfig.beans.builder;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.EnableAutoConfiguration;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.GenericBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.AutowiredDescription;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.property.PropertyValue;
import com.kfyty.loveqq.framework.core.io.FactoriesLoader;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.lang.reflect.Method;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBeanDefinition.FACTORY_BEAN_PREFIX;
import static com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBeanDefinition.addFactoryBeanCache;
import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.newInstance;

/**
 * 描述: 创建 bean 定义
 *
 * @author kfyty725
 * @date 2021/8/6 22:09
 * @email kfyty725@hotmail.com
 */
public class BeanDefinitionBuilder {
    private final GenericBeanDefinition beanDefinition;

    private BeanDefinitionBuilder(GenericBeanDefinition beanDefinition) {
        this.beanDefinition = beanDefinition;
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

    public BeanDefinitionBuilder setInitMethod(String initMethod) {
        this.beanDefinition.setInitMethod(initMethod);
        return this;
    }

    public BeanDefinitionBuilder setDestroyMethod(String destroyMethod) {
        this.beanDefinition.setDestroyMethod(destroyMethod);
        return this;
    }

    public BeanDefinitionBuilder addConstructorArgs(Class<?> argType, Object arg) {
        this.beanDefinition.addConstructorArgs(argType, arg);
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
        if (this.beanDefinition.isFactoryBean()) {
            this.beanDefinition.setBeanName(FACTORY_BEAN_PREFIX + this.beanDefinition.getBeanName());
        }
        return this.beanDefinition;
    }

    public void validate() {
        if (CommonUtil.empty(this.beanDefinition.getBeanName())) {
            throw new IllegalStateException("The bean name can't empty !");
        }
        if (this.beanDefinition.getBeanType() == null) {
            throw new IllegalStateException("The bean type can't empty !");
        }
        if (this.beanDefinition.isFactoryBean()) {
            FactoryBean<?> factoryBean = (FactoryBean<?>) newInstance(this.beanDefinition.getBeanType(), this.beanDefinition.getDefaultConstructArgs());
            if (!this.beanDefinition.isMethodBean() && this.beanDefinition.getBeanName().equals(resolveBeanName(this.beanDefinition.getBeanType()))) {
                // 如果是 class 定义 FactoryBean，且是默认名称，则更新名称为实际 bean 类型的默认名称，否则会冲突
                this.setBeanName(resolveBeanName(factoryBean.getBeanType()));
            }
            addFactoryBeanCache(FACTORY_BEAN_PREFIX + this.beanDefinition.getBeanName(), factoryBean);
        }
    }

    public static BeanDefinitionBuilder genericBeanDefinition() {
        return new BeanDefinitionBuilder(new GenericBeanDefinition());
    }

    public static BeanDefinitionBuilder genericBeanDefinition(Class<?> beanType) {
        return genericBeanDefinition(resolveBeanName(beanType), beanType);
    }

    public static BeanDefinitionBuilder genericBeanDefinition(String beanName, Class<?> beanType) {
        return new BeanDefinitionBuilder(new GenericBeanDefinition(beanName, beanType));
    }

    public static BeanDefinitionBuilder genericBeanDefinition(BeanDefinition source, Method beanMethod, Bean bean) {
        MethodBeanDefinition beanDefinition = new MethodBeanDefinition(BeanUtil.getBeanName(beanMethod, bean), beanMethod.getReturnType(), source, beanMethod);
        if (CommonUtil.notEmpty(bean.initMethod())) {
            beanDefinition.setInitMethod(bean.initMethod());
        }
        if (CommonUtil.notEmpty(bean.destroyMethod())) {
            beanDefinition.setDestroyMethod(bean.destroyMethod());
        }
        return new BeanDefinitionBuilder(beanDefinition);
    }

    public static BeanDefinitionBuilder genericBeanDefinition(BeanDefinition factoryBeanDefinition) {
        return new BeanDefinitionBuilder(new FactoryBeanDefinition(factoryBeanDefinition));
    }

    public static String resolveBeanName(Class<?> beanType) {
        Component component = AnnotationUtil.findAnnotation(beanType, Component.class);
        if (component != null && CommonUtil.notEmpty(component.value())) {
            return component.value();
        }
        if (FactoriesLoader.loadFactories(EnableAutoConfiguration.class).contains(beanType.getName())) {
            return beanType.getName();
        }
        return BeanUtil.getBeanName(beanType);
    }
}
