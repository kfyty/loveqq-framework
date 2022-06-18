package com.kfyty.boot.processor;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.support.autoconfig.PropertyContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.support.autoconfig.annotation.NestedConfigurationProperty;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.ReflectUtil;

import java.lang.reflect.Field;
import java.util.Map;

import static com.kfyty.support.utils.AnnotationUtil.hasAnnotation;
import static com.kfyty.support.utils.ConverterUtil.getTypeConverter;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/5/25 22:38
 * @email kfyty725@hotmail.com
 */
@Component
public class ConfigurationPropertiesBeanPostProcessor implements InstantiationAwareBeanPostProcessor {
    @Autowired
    private PropertyContext propertyContext;

    @Autowired
    private ApplicationContext applicationContext;

    public Object postProcessAfterInstantiation(Object bean, String beanName) {
        ConfigurationProperties configurationProperties = this.obtainConfigurationPropertiesAnnotation(beanName);
        if (configurationProperties != null) {
            this.bindConfigurationProperties(AopUtil.getTarget(bean), configurationProperties.value(), configurationProperties.ignoreInvalidFields(), configurationProperties.ignoreUnknownFields());
        }
        return null;
    }

    protected ConfigurationProperties obtainConfigurationPropertiesAnnotation(String beanName) {
        BeanDefinition beanDefinition = this.applicationContext.getBeanDefinition(beanName);
        if (beanDefinition instanceof MethodBeanDefinition) {
            return AnnotationUtil.findAnnotation(((MethodBeanDefinition) beanDefinition).getBeanMethod(), ConfigurationProperties.class);
        }
        return AnnotationUtil.findAnnotation(beanDefinition.getBeanType(), ConfigurationProperties.class);
    }

    protected <T extends Enum<T>> void bindConfigurationProperties(Object bean, String prefix, boolean ignoreInvalidFields, boolean ignoreUnknownFields) {
        for (Map.Entry<String, Field> entry : ReflectUtil.getFieldMap(bean.getClass()).entrySet()) {
            String key = prefix + "." + entry.getKey();
            Field field = entry.getValue();
            if (hasAnnotation(field, NestedConfigurationProperty.class)) {
                if (this.propertyContext.getProperties().keySet().stream().anyMatch(e -> e.startsWith(key))) {
                    Object fieldInstance = ReflectUtil.newInstance(field.getType());
                    ReflectUtil.setFieldValue(bean, field, fieldInstance);
                    this.bindConfigurationProperties(fieldInstance, key, ignoreInvalidFields, ignoreUnknownFields);
                }
                continue;
            }
            if (!this.propertyContext.contains(key) || !field.getType().isEnum() && getTypeConverter(String.class, field.getType()) == null) {
                if (ignoreUnknownFields) {
                    continue;
                }
                throw new IllegalArgumentException("configuration properties bind failed, property key: [" + key + "] not found or no suitable converter");
            }
            if (!field.getType().isEnum()) {
                ReflectUtil.setFieldValue(bean, field, this.propertyContext.getProperty(key, field.getType()));
                continue;
            }
            @SuppressWarnings("unchecked")
            T enumValue = Enum.valueOf((Class<T>) field.getType(), this.propertyContext.getProperty(key, String.class));
            ReflectUtil.setFieldValue(bean, field, enumValue);
        }
    }
}
