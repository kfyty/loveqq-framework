package com.kfyty.support.utils;

import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.autoconfig.annotation.Qualifier;
import com.kfyty.support.autoconfig.annotation.Scope;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.FactoryBeanDefinition;
import com.kfyty.support.autoconfig.beans.GenericBeanDefinition;
import com.kfyty.support.autoconfig.beans.MethodBeanDefinition;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * 描述: bean 工具
 *
 * @author kfyty725
 * @date 2021/6/3 9:48
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class BeanUtil {

    public static String convert2BeanName(Class<?> clazz) {
        return convert2BeanName(clazz.getSimpleName());
    }

    public static String convert2BeanName(String className) {
        if(className.length() > 1 && Character.isUpperCase(className.charAt(1))) {
            return className;
        }
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    public static String getBeanName(Parameter parameter) {
        Qualifier qualifier = AnnotationUtil.findAnnotation(parameter, Qualifier.class);
        return qualifier != null ? qualifier.value() : getBeanName(parameter.getType(), AnnotationUtil.findAnnotation(parameter, Autowired.class));
    }

    public static String getBeanName(Method method, Bean bean) {
        return bean != null && CommonUtil.notEmpty(bean.value()) ? bean.value() : method.getName();
    }

    public static String getBeanName(Class<?> clazz, Autowired autowired) {
        return autowired != null && CommonUtil.notEmpty(autowired.value()) ? autowired.value() : convert2BeanName(clazz);
    }

    public static boolean isSingleton(Class<?> clazz) {
        Scope scope = AnnotationUtil.findAnnotation(clazz, Scope.class);
        return scope == null || BeanDefinition.SCOPE_SINGLETON.equals(scope.value());
    }

    public static boolean isSingleton(Method beanMethod) {
        Scope scope = AnnotationUtil.findAnnotation(beanMethod, Scope.class);
        return scope == null || BeanDefinition.SCOPE_SINGLETON.equals(scope.value());
    }

    public static int getBeanOrder(Object bean) {
        return Optional.ofNullable(AnnotationUtil.findAnnotation(bean, Order.class)).map(Order::value).orElse(Order.LOWEST_PRECEDENCE);
    }

    public static int getBeanOrder(BeanDefinition beanDefinition) {
        if(beanDefinition instanceof FactoryBeanDefinition) {
            return getBeanOrder(((FactoryBeanDefinition) beanDefinition).getFactoryBeanDefinition());
        }
        if(beanDefinition instanceof MethodBeanDefinition) {
            Order order = AnnotationUtil.findAnnotation(((MethodBeanDefinition) beanDefinition).getBeanMethod(), Order.class);
            return order != null ? order.value() : Order.LOWEST_PRECEDENCE;
        }
        if(beanDefinition instanceof GenericBeanDefinition) {
            Order order = AnnotationUtil.findAnnotation(beanDefinition.getBeanType(), Order.class);
            return order != null ? order.value() : Order.LOWEST_PRECEDENCE;
        }
        return Order.LOWEST_PRECEDENCE;
    }

    public static <S, T> T copyBean(S source, T target) {
        return copyBean(source, target, (f, v) -> !Modifier.isFinal(f.getModifiers()));
    }

    public static <T> T copyProperties(Map<String, Object> map, Class<T> clazz) {
        return copyProperties(map, clazz, (f, v) -> !Modifier.isFinal(f.getModifiers()));
    }

    public static Map<String, Object> copyProperties(Object obj) {
        return copyProperties(obj, (f, v) -> true);
    }

    public static <S, T> T copyBean(S source, T target, BiPredicate<Field, Object> fieldValTest) {
        Map<String, Field> sourceFileMap = ReflectUtil.getFieldMap(source.getClass());
        Map<String, Field> targetFieldMap = ReflectUtil.getFieldMap(target.getClass());
        for (Map.Entry<String, Field> fieldEntry : sourceFileMap.entrySet()) {
            if(!targetFieldMap.containsKey(fieldEntry.getKey())) {
                log.warn("cannot copy bean from [{}] to [{}], no field found from target bean !", source.getClass(), target.getClass());
                continue;
            }
            Field field = targetFieldMap.get(fieldEntry.getKey());
            Object fieldValue = ReflectUtil.getFieldValue(source, fieldEntry.getValue());
            if(!fieldValTest.test(field, fieldValue)) {
                LogUtil.logIfDebugEnabled((log, param) -> log.debug("copy bean skip field: {}", param), fieldEntry.getValue());
                continue;
            }
            ReflectUtil.setFieldValue(target, field, fieldValue);
        }
        return target;
    }

    public static <T> T copyProperties(Map<String, Object> map, Class<T> clazz, BiPredicate<Field, Object> fieldValTest) {
        if(CommonUtil.empty(map) || clazz == null) {
            return null;
        }
        T o = ReflectUtil.newInstance(clazz);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Field field = ReflectUtil.getField(clazz, entry.getKey());
            if(field == null) {
                log.warn("cannot copy properties [{}], no field found from target class [{}] !", entry.getKey(), clazz);
                continue;
            }
            if(!fieldValTest.test(field, entry.getValue())) {
                LogUtil.logIfDebugEnabled((log, param) -> log.debug("copy properties skip field: {}", param), field);
                continue;
            }
            ReflectUtil.setFieldValue(o, field, entry.getValue());
        }
        return o;
    }

    public static Map<String, Object> copyProperties(Object obj, BiPredicate<Field, Object> fieldValTest) {
        if(obj == null) {
            return new HashMap<>(2);
        }
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Field> entry : ReflectUtil.getFieldMap(obj.getClass()).entrySet()) {
            Object fieldValue = ReflectUtil.getFieldValue(obj, entry.getValue());
            if(!fieldValTest.test(entry.getValue(), entry.getValue())) {
                LogUtil.logIfDebugEnabled((log, param) -> log.debug("copy properties skip field: {}", param), entry.getValue());
                continue;
            }
            map.put(entry.getKey(), fieldValue);
        }
        return map;
    }
}
