package com.kfyty.boot.processor;

import com.kfyty.core.autoconfig.GenericPropertiesContext;
import com.kfyty.core.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.autoconfig.annotation.Value;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.BeanUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.core.wrapper.Pair;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Map;

import static com.kfyty.core.utils.ReflectUtil.setFieldValue;

/**
 * 功能描述: Value 注解处理器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/27 10:43
 * @since JDK 1.8
 */
@Slf4j
@Component
@Order(Order.HIGHEST_PRECEDENCE)
public class ValueAnnotationBeanPostProcessor implements InstantiationAwareBeanPostProcessor {
    @Autowired
    protected GenericPropertiesContext propertyContext;

    @Override
    public Object postProcessAfterInstantiation(Object bean, String beanName) {
        Object target = AopUtil.getTarget(bean);
        Class<?> targetClass = target.getClass();
        for (Map.Entry<String, Field> entry : ReflectUtil.getFieldMap(targetClass).entrySet()) {
            Field field = entry.getValue();
            Value annotation = AnnotationUtil.findAnnotation(field, Value.class);
            if (annotation == null) {
                continue;
            }
            Pair<String, String> resolve = this.resolve(annotation.value());
            Object property = this.propertyContext.getProperty(resolve.getKey(), field.getGenericType());
            if (property == null && resolve.getValue() == null) {
                throw new IllegalArgumentException("parameter does not exist: " + resolve.getKey());
            }
            if (property == null) {
                this.propertyContext.setProperty(resolve.getKey(), resolve.getValue());
                property = this.propertyContext.getProperty(resolve.getKey(), field.getGenericType());
            }
            setFieldValue(target, field, property);
        }
        if (AnnotationUtil.hasAnnotationElement(targetClass, Configuration.class)) {
            BeanUtil.copyProperties(target, bean);
        }
        return null;
    }

    protected Pair<String, String> resolve(String value) {
        int index = value.indexOf(':');
        if (index < 0) {
            return new Pair<>(value.replaceAll("[${}]", ""), null);
        }
        return new Pair<>(value.substring(0, index).replaceAll("[${}]", ""), value.substring(index + 1).replaceAll("[${}]", ""));
    }
}
