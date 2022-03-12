package com.kfyty.boot.processor;

import com.kfyty.support.autoconfig.InitializingBean;
import com.kfyty.support.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.support.autoconfig.PropertyContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.autoconfig.annotation.Value;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Map;

import static com.kfyty.support.utils.ConverterUtil.convert;

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
        Class<?> targetClass = AopUtil.getTargetClass(bean);
        for (Map.Entry<String, Field> entry : ReflectUtil.getFieldMap(targetClass).entrySet()) {
            Field field = entry.getValue();
            Value annotation = AnnotationUtil.findAnnotation(field, Value.class);
            if (annotation == null) {
                continue;
            }
            String key = annotation.value().replaceAll("[${}]", "");
            Object property = this.propertyContext.getProperty(key, field.getType());
            ReflectUtil.setFieldValue(bean, field, property != null ? property : convert(annotation.defaultValue(), field.getType()));
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() {
        this.propertyContext.loadProperties();
    }
}
