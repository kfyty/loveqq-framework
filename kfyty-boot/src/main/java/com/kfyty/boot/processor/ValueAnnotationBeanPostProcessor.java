package com.kfyty.boot.processor;

import com.kfyty.core.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.autoconfig.annotation.Value;
import com.kfyty.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.core.autoconfig.env.PlaceholdersResolver;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.BeanUtil;
import com.kfyty.core.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Map;

import static com.kfyty.core.autoconfig.beans.GenericBeanDefinition.resolvePlaceholderValue;
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
    protected PlaceholdersResolver placeholdersResolver;

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
            Object property = resolvePlaceholderValue(annotation.value(), field.getGenericType(), this.placeholdersResolver, this.propertyContext);
            if (property != null) {
                setFieldValue(target, field, property);
            }
        }
        if (AnnotationUtil.hasAnnotationElement(targetClass, Configuration.class)) {
            BeanUtil.copyProperties(target, bean);
        }
        return null;
    }
}
