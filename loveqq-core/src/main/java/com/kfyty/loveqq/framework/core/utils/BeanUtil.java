package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.autoconfig.Ordered;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.ConditionalBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.GenericBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.MethodBeanDefinition;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Optional.ofNullable;

/**
 * 描述: bean 工具
 *
 * @author kfyty725
 * @date 2021/6/3 9:48
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class BeanUtil {
    /**
     * 默认的复制属性断言
     */
    public static final BiPredicate<Field, Object> DEFAULT_COPY_PREDICATE = (f, v) -> !(isStatic(f.getModifiers()) && isFinal(f.getModifiers()));

    /**
     * 作用域代理原 bean 名称前缀
     */
    public static final String SCOPE_PROXY_SOURCE_PREFIX = "scopedTarget.";

    /**
     * 延迟初始化代理原 bean 名称前缀
     */
    public static final String LAZY_PROXY_SOURCE_PREFIX = "laziedTarget.";

    /**
     * 移除 FactoryBean 名称前缀
     *
     * @param beanName factory bean name
     * @return 原 bean name
     */
    public static String removeFactoryBeanNamePrefix(String beanName) {
        while (beanName.startsWith(FactoryBeanDefinition.FACTORY_BEAN_PREFIX)) {
            beanName = beanName.substring(FactoryBeanDefinition.FACTORY_BEAN_PREFIX.length());
        }
        return beanName;
    }

    /**
     * 根据 class 对象转换为 bean name
     *
     * @param clazz class
     * @return bean name
     */
    public static String getBeanName(Class<?> clazz) {
        return getBeanName(clazz.getSimpleName());
    }

    /**
     * 将字符串转换为 bean name
     *
     * @param className 一般为类名
     * @return bean name
     */
    public static String getBeanName(String className) {
        if (className.length() > 1 && Character.isUpperCase(className.charAt(1))) {
            return className;
        }
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    /**
     * 从方法对象以及 Bean 对象中解析 bean name
     *
     * @param method 方法对象
     * @param bean   Bean 注解实例
     * @return bean name
     */
    public static String getBeanName(Method method, Bean bean) {
        return bean == null || bean.value().isEmpty() ? method.getName() : bean.value();
    }

    /**
     * 从 class 以及 Autowired 注解中解析 bean name
     *
     * @param clazz            class
     * @param possibleBeanName 可能的自动注入名称
     * @return bean name
     */
    public static String getBeanName(Class<?> clazz, String possibleBeanName) {
        return CommonUtil.notEmpty(possibleBeanName) ? possibleBeanName : getBeanName(clazz);
    }

    /**
     * 获取给定 bean 的优先级
     *
     * @param bean bean
     * @return order，默认 {@link Order#DEFAULT_PRECEDENCE}
     */
    public static int getBeanOrder(Object bean) {
        if (bean instanceof Ordered) {
            return ((Ordered) bean).getOrder();
        }
        return ofNullable(AnnotationUtil.findAnnotation(bean, Order.class)).map(Order::value).orElse(Order.DEFAULT_PRECEDENCE);
    }

    /**
     * 获取给定 BeanDefinition 的优先级
     *
     * @param beanDefinition bean 定义
     * @return order，默认 {@link Order#DEFAULT_PRECEDENCE}
     */
    public static int getBeanOrder(BeanDefinition beanDefinition) {
        if (beanDefinition instanceof ConditionalBeanDefinition) {
            return getBeanOrder(((ConditionalBeanDefinition) beanDefinition).getBeanDefinition());
        }
        if (beanDefinition instanceof FactoryBeanDefinition) {
            return getBeanOrder(((FactoryBeanDefinition) beanDefinition).getFactoryBeanDefinition());
        }
        if (beanDefinition instanceof MethodBeanDefinition) {
            Order order = AnnotationUtil.findAnnotation(beanDefinition.getBeanMethod(), Order.class);
            return order != null ? order.value() : Order.DEFAULT_PRECEDENCE;
        }
        if (beanDefinition instanceof GenericBeanDefinition) {
            Order order = AnnotationUtil.findAnnotation(beanDefinition.getBeanType(), Order.class);
            return order != null ? order.value() : Order.DEFAULT_PRECEDENCE;
        }
        return Order.DEFAULT_PRECEDENCE;
    }

    /**
     * 将对象的属性复制到 Map 中
     *
     * @param obj 目标对象
     * @return Map
     */
    public static Map<String, Object> copyProperties(Object obj) {
        return copyProperties(obj, (f, v) -> true);
    }

    /**
     * 复制 bean 属性
     *
     * @param source 源对象
     * @param target 目标对象
     * @return 目标对象
     */
    public static <S, T> T copyProperties(S source, T target) {
        return copyProperties(source, target, DEFAULT_COPY_PREDICATE);
    }

    /**
     * 将 Map 中的数据复制到 bean 中
     *
     * @param map   Map
     * @param clazz 目标 bean class 类型
     * @return 目标对象
     */
    public static <T> T bindProperties(Map<String, Object> map, Class<T> clazz) {
        return bindProperties(map, clazz, DEFAULT_COPY_PREDICATE);
    }

    /**
     * 将对象的属性复制到 Map 中，允许通过属性过滤器控制
     *
     * @param obj          目标对象
     * @param fieldValTest 属性过滤器
     * @return Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> copyProperties(Object obj, BiPredicate<Field, Object> fieldValTest) {
        if (obj == null) {
            return Collections.emptyMap();
        }
        if (obj instanceof Map<?, ?>) {
            return (Map<String, Object>) obj;
        }
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Field> entry : ReflectUtil.getFieldMap(obj.getClass()).entrySet()) {
            Object fieldValue = ReflectUtil.getFieldValue(obj, entry.getValue());
            if (fieldValTest.test(entry.getValue(), fieldValue)) {
                map.put(entry.getKey(), fieldValue);
            } else {
                LogUtil.logIfDebugEnabled(log, log -> log.debug("copy properties skip field: {}", entry.getValue()));
            }
        }
        return map;
    }

    /**
     * 使用反射复制 bean 属性，允许通过属性过滤器控制
     *
     * @param source       源对象
     * @param target       目标对象
     * @param fieldValTest 属性过滤器
     * @return 目标对象
     */
    public static <S, T> T copyProperties(S source, T target, BiPredicate<Field, Object> fieldValTest) {
        Map<String, Field> sourceFileMap = ReflectUtil.getFieldMap(source.getClass());
        Map<String, Field> targetFieldMap = ReflectUtil.getFieldMap(target.getClass());
        for (Map.Entry<String, Field> entry : sourceFileMap.entrySet()) {
            if (!targetFieldMap.containsKey(entry.getKey())) {
                LogUtil.logIfWarnEnabled(log, log -> log.warn("cannot copy bean from [{}] to [{}], there's not field found from target bean !", source.getClass(), target.getClass()));
                continue;
            }
            Field field = targetFieldMap.get(entry.getKey());
            Object fieldValue = ReflectUtil.getFieldValue(source, entry.getValue());
            if (fieldValTest.test(field, fieldValue)) {
                ReflectUtil.setFieldValue(target, field, fieldValue);
            } else {
                LogUtil.logIfDebugEnabled(log, log -> log.debug("copy properties skip field: {}", entry.getValue()));
            }
        }
        return target;
    }

    /**
     * 将 Map 中的数据复制到 bean 中，允许通过属性过滤器控制
     *
     * @param map          Map
     * @param clazz        目标 bean class 类型
     * @param fieldValTest 属性过滤器
     * @return 目标对象
     */
    public static <T> T bindProperties(Map<String, Object> map, Class<T> clazz, BiPredicate<Field, Object> fieldValTest) {
        if (CommonUtil.empty(map) || clazz == null) {
            return null;
        }
        T o = ReflectUtil.newInstance(clazz);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Field field = ReflectUtil.getField(clazz, entry.getKey());
            if (field == null) {
                LogUtil.logIfWarnEnabled(log, log -> log.warn("cannot bind properties [{}], no field found from target class [{}] !", entry.getKey(), clazz));
                continue;
            }
            if (fieldValTest.test(field, entry.getValue())) {
                if (field.getType().isInstance(entry.getValue())) {
                    ReflectUtil.setFieldValue(o, field, entry.getValue());
                } else {
                    ReflectUtil.setFieldValue(o, field, ConverterUtil.convert(entry.getValue(), field.getType()));
                }
            } else {
                LogUtil.logIfDebugEnabled(log, log -> log.debug("bind properties skip field: {}", field));
            }
        }
        return o;
    }
}
