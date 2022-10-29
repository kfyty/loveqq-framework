package com.kfyty.boot.processor;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.GenericPropertiesContext;
import com.kfyty.core.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.core.autoconfig.annotation.NestedConfigurationProperty;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.ReflectUtil;

import java.lang.reflect.Field;
import java.util.Map;

import static com.kfyty.core.utils.AnnotationUtil.hasAnnotation;

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
    private ApplicationContext applicationContext;

    @Autowired
    private GenericPropertiesContext propertyContext;

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

    public <T extends Enum<T>> void bindConfigurationProperties(Object bean, String prefix, boolean ignoreInvalidFields, boolean ignoreUnknownFields) {
        for (Map.Entry<String, Field> entry : ReflectUtil.getFieldMap(bean.getClass()).entrySet()) {
            String key = prefix + "." + entry.getKey();
            Field field = entry.getValue();
            if (ReflectUtil.isStaticFinal(field.getModifiers())) {
                continue;
            }
            if (hasAnnotation(field, NestedConfigurationProperty.class)) {
                if (this.propertyContext.getProperties().keySet().stream().anyMatch(e -> e.startsWith(key))) {
                    Object fieldInstance = ReflectUtil.newInstance(field.getType());
                    ReflectUtil.setFieldValue(bean, field, fieldInstance);
                    this.bindConfigurationProperties(fieldInstance, key, ignoreInvalidFields, ignoreUnknownFields);
                }
                continue;
            }
            if (!Map.class.isAssignableFrom(field.getType()) && !this.propertyContext.contains(key)) {
                if (ignoreUnknownFields) {
                    continue;
                }
                throw new IllegalArgumentException("configuration properties bind failed, property key: [" + key + "] not exists");
            }
            if (field.getType().isEnum()) {
                @SuppressWarnings("unchecked")
                T enumValue = Enum.valueOf((Class<T>) field.getType(), this.propertyContext.getProperty(key, String.class));
                ReflectUtil.setFieldValue(bean, field, enumValue);
                continue;
            }
            try {
                ReflectUtil.setFieldValue(bean, field, this.propertyContext.getProperty(key, field.getGenericType()));
            } catch (Exception e) {
                if (ignoreInvalidFields) {
                    continue;
                }
                throw new IllegalArgumentException("configuration properties bind failed, property key: [" + key + "]", e);
            }
        }
    }
}
