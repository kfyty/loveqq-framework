package com.kfyty.boot.processor;

import com.kfyty.support.autoconfig.InitializingBean;
import com.kfyty.support.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.support.autoconfig.PropertyContext;
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
public class ValueAnnotationBeanPostProcessor implements InstantiationAwareBeanPostProcessor, InitializingBean {
    @Autowired
    private PropertyContext propertyContext;

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
            String key = annotation.value().replaceAll("[${}]", "");
            Object property = this.propertyContext.getProperty(key, field.getType());
            if (property == null && CommonUtil.empty(annotation.defaultValue())) {
                throw new IllegalArgumentException("parameter does not exist: " + key);
            }
            setFieldValue(target, field, property != null ? property : convert(annotation.defaultValue(), field.getType()));
        }
        if (AnnotationUtil.hasAnnotationElement(targetClass, Configuration.class)) {
            BeanUtil.copyProperties(target, bean);
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() {
        this.propertyContext.loadProperties();
    }
}
