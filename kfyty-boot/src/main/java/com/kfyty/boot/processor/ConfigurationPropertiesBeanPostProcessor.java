package com.kfyty.boot.processor;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.GenericPropertiesContext;
import com.kfyty.core.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.core.autoconfig.annotation.Lazy;
import com.kfyty.core.autoconfig.annotation.NestedConfigurationProperty;
import com.kfyty.core.autoconfig.annotation.Value;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.ReflectUtil;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import static com.kfyty.core.utils.AnnotationUtil.hasAnnotation;

/**
 * 描述: 绑定 bean 属性配置
 * <p>
 * <b>
 * 本类不能实现 {@link com.kfyty.core.autoconfig.aware.ApplicationContextAware} 之类的接口
 * 否则 {@link Value} 注解将失效
 * </b>
 * </p>
 *
 * @author kfyty725
 * @date 2022/5/25 22:38
 * @email kfyty725@hotmail.com
 */
@Lazy
@Getter
@Component
public class ConfigurationPropertiesBeanPostProcessor implements InstantiationAwareBeanPostProcessor {
    @Value("${k.config.property.bind.internal.ignore-invalid-fields:false}")
    protected Boolean ignoreInvalidFields;

    @Value("${k.config.property.bind.internal.ignore-unknown-fields:true}")
    protected Boolean ignoreUnknownFields;

    @Value("${k.config.property.bind.internal.delimiter:,}")
    protected String bindPropertyDelimiter;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected GenericPropertiesContext propertyContext;

    public Object postProcessAfterInstantiation(Object bean, String beanName) {
        if (this == bean) {
            return null;
        }
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

    public void bindConfigurationProperties(Object bean, String prefix) {
        this.bindConfigurationProperties(bean, prefix, this.ignoreInvalidFields, this.ignoreUnknownFields);
    }

    public void bindConfigurationProperties(Object bean, String prefix, boolean ignoreInvalidFields, boolean ignoreUnknownFields) {
        for (Map.Entry<String, Field> entry : ReflectUtil.getFieldMap(bean.getClass()).entrySet()) {
            if (ReflectUtil.isStaticFinal(entry.getValue().getModifiers())) {
                continue;
            }
            String key = prefix + "." + entry.getKey();
            this.bindConfigurationProperties(bean, key, entry.getValue(), ignoreInvalidFields, ignoreUnknownFields);
        }
    }

    public <T extends Enum<T>> void bindConfigurationProperties(Object bean, String key, Field field, boolean ignoreInvalidFields, boolean ignoreUnknownFields) {
        if (hasAnnotation(field, NestedConfigurationProperty.class)) {
            if (this.propertyContext.getProperties().keySet().stream().anyMatch(e -> e.startsWith(key))) {
                Object fieldInstance = ReflectUtil.newInstance(field.getType());
                ReflectUtil.setFieldValue(bean, field, fieldInstance);
                this.bindConfigurationProperties(fieldInstance, key, ignoreInvalidFields, ignoreUnknownFields);
            }
            return;
        }

        if (!this.propertyContext.contains(key) && !(this.isMapProperties(key, field) || this.isCollectionProperties(key, field))) {
            if (ignoreUnknownFields) {
                return;
            }
            throw new IllegalArgumentException("configuration properties bind failed, property key: [" + key + "] not exists");
        }

        if (field.getType().isEnum()) {
            @SuppressWarnings("unchecked")
            T enumValue = Enum.valueOf((Class<T>) field.getType(), this.propertyContext.getProperty(key, String.class));
            ReflectUtil.setFieldValue(bean, field, enumValue);
            return;
        }

        try {
            Object property = this.propertyContext.getProperty(key, field.getGenericType());
            if (property != null) {
                ReflectUtil.setFieldValue(bean, field, property);
            }
        } catch (Exception e) {
            if (ignoreInvalidFields) {
                return;
            }
            throw new IllegalArgumentException("configuration properties bind failed, property key: [" + key + "]", e);
        }
    }

    protected boolean isMapProperties(String key, Field field) {
        return Map.class.isAssignableFrom(field.getType());
    }

    protected boolean isCollectionProperties(String key, Field field) {
        return field.getType().isArray() || Collection.class.isAssignableFrom(field.getType());
    }
}
