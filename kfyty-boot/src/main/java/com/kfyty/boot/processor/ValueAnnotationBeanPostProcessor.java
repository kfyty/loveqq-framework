package com.kfyty.boot.processor;

import com.kfyty.support.autoconfig.GenericPropertiesContext;
import com.kfyty.support.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.autoconfig.annotation.Value;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Map;

import static com.kfyty.support.utils.ConverterUtil.convert;
import static com.kfyty.support.utils.ReflectUtil.setFieldValue;

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
    private GenericPropertiesContext propertyContext;

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
            setFieldValue(target, field, property != null ? property : convert(resolve.getValue(), field.getType()));
        }
        if (AnnotationUtil.hasAnnotationElement(targetClass, Configuration.class)) {
            BeanUtil.copyProperties(target, bean);
        }
        return null;
    }

    private Pair<String, String> resolve(String value) {
        int index = value.indexOf(value, ':');
        if (index < 0 || index == value.length() - 1) {
            return new Pair<>(value.replaceAll("[${}]", ""), index < 0 ? null : CommonUtil.EMPTY_STRING);
        }
        return new Pair<>(value.substring(0, index).replaceAll("[${}]", ""), value.substring(index + 1));
    }
}
