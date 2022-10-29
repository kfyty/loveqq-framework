package com.kfyty.boot.processor;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.ApplicationContextAware;
import com.kfyty.core.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.Lazy;
import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.autoconfig.beans.AutowiredCapableSupport;
import com.kfyty.core.autoconfig.beans.autowired.AutowiredDescription;
import com.kfyty.core.autoconfig.beans.autowired.AutowiredProcessor;
import com.kfyty.core.generic.ActualGeneric;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.BeanUtil;
import com.kfyty.core.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kfyty.core.utils.AnnotationUtil.hasAnnotation;

/**
 * 功能描述: Autowired 注解处理器
 * 必须实现 InstantiationAwareBeanPostProcessor 接口，以保证其最高的优先级
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/27 10:43
 * @since JDK 1.8
 */
@Slf4j
@Order(Integer.MIN_VALUE)
@Component(AutowiredCapableSupport.BEAN_NAME)
public class AutowiredAnnotationBeanPostProcessor implements ApplicationContextAware, InstantiationAwareBeanPostProcessor, AutowiredCapableSupport {
    /**
     * 自动注入处理器
     */
    private AutowiredProcessor autowiredProcessor;

    /**
     * 懒注入缓存
     */
    private Map<Object, AccessibleObject> lazyAutowired;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.autowiredProcessor = new AutowiredProcessor(context);
        this.autowiredBean(context);
    }

    @Override
    public void autowiredBean(Object bean) {
        this.autowiredBean(bean, false);
    }

    @Override
    public void autowiredBean(Object bean, boolean ignoredLazy) {
        Object target = AopUtil.getTarget(bean);
        Class<?> targetClass = target.getClass();
        this.autowiredBeanField(targetClass, target, ignoredLazy);
        this.autowiredBeanMethod(targetClass, target, ignoredLazy);
        if (AnnotationUtil.hasAnnotationElement(targetClass, Configuration.class)) {
            BeanUtil.copyProperties(target, bean);
        }
    }

    @Override
    public void autowiredLazy() {
        for (Map.Entry<Object, AccessibleObject> entry : this.lazyAutowired.entrySet()) {
            if (entry.getValue() instanceof Field) {
                this.autowiredProcessor.doAutowired(entry.getKey(), (Field) entry.getValue());
                continue;
            }
            this.autowiredProcessor.doAutowired(entry.getKey(), (Method) entry.getValue());
        }
        this.lazyAutowired.clear();
        this.lazyAutowired = null;
    }

    protected void autowiredBeanField(Class<?> clazz, Object bean, boolean ignoredLazy) {
        List<Field> laziedFields = new ArrayList<>(4);
        List<Method> beanMethods = ReflectUtil.getMethods(clazz).stream().filter(e -> hasAnnotation(e, Bean.class)).collect(Collectors.toList());
        for (Field field : ReflectUtil.getFieldMap(clazz).values()) {
            AutowiredDescription description = AutowiredDescription.from(field);
            if (description == null) {
                continue;
            }
            if (!ignoredLazy && hasAnnotation(field, Lazy.class)) {
                this.putLazy(bean, field);
                continue;
            }
            ActualGeneric actualGeneric = ActualGeneric.from(clazz, field);
            if (beanMethods.stream().anyMatch(e -> actualGeneric.getSimpleActualType().isAssignableFrom(e.getReturnType()))) {
                laziedFields.add(field);
                continue;
            }
            this.autowiredProcessor.doAutowired(bean, field, description);
        }
        laziedFields.forEach(e -> this.autowiredProcessor.doAutowired(bean, e));
    }

    protected void autowiredBeanMethod(Class<?> clazz, Object bean, boolean ignoredLazy) {
        for (Method method : ReflectUtil.getMethods(clazz)) {
            AutowiredDescription description = AutowiredDescription.from(method);
            if (description == null) {
                continue;
            }
            if (!ignoredLazy && hasAnnotation(method, Lazy.class)) {
                this.putLazy(bean, method);
                continue;
            }
            this.autowiredProcessor.doAutowired(bean, method, description);
        }
    }

    protected void putLazy(Object bean, AccessibleObject lazy) {
        if (this.lazyAutowired == null) {
            this.lazyAutowired = new HashMap<>();
        }
        this.lazyAutowired.put(bean, lazy);
    }
}
