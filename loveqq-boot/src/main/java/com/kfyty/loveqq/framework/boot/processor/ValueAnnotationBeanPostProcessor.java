package com.kfyty.loveqq.framework.boot.processor;

import com.kfyty.loveqq.framework.core.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.loveqq.framework.core.autoconfig.env.PlaceholdersResolver;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;

import java.lang.reflect.Field;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.GenericBeanDefinition.resolvePlaceholderValue;
import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.setFieldValue;

/**
 * 功能描述: Value 注解处理器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/27 10:43
 * @since JDK 1.8
 */
@Component
@Order(Order.HIGHEST_PRECEDENCE)
public class ValueAnnotationBeanPostProcessor implements InstantiationAwareBeanPostProcessor {
    @Autowired
    protected PlaceholdersResolver placeholdersResolver;

    @Autowired
    protected GenericPropertiesContext propertyContext;

    @Override
    public Object postProcessAfterInstantiation(Object bean, String beanName, BeanDefinition beanDefinition) {
        if (MethodBeanDefinition.isIgnoredAutowired(beanDefinition)) {
            return null;
        }
        Object target = AopUtil.getTarget(bean);
        Class<?> targetClass = target.getClass();
        for (Field field : ReflectUtil.getFields(targetClass)) {
            Value annotation = AnnotationUtil.findAnnotation(field, Value.class);
            if (annotation == null) {
                continue;
            }
            Object property = resolvePlaceholderValue(annotation.value(), field.getGenericType(), this.placeholdersResolver, this.propertyContext);
            if (property != null) {
                setFieldValue(target, field, property);
                if (bean != target && AopUtil.isCglibProxy(bean)) {
                    setFieldValue(bean, field, property);
                }
            }
        }
        return null;
    }
}
