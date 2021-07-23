package com.kfyty.boot.processor;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.Lazy;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.autoconfig.beans.AutowiredCapableSupport;
import com.kfyty.support.autoconfig.beans.AutowiredProcessor;
import com.kfyty.support.generic.ActualGeneric;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private AutowiredProcessor autowiredProcessor;

    private final Map<Object, Field> lazyAutowiredField = new HashMap<>();
    private final Map<Object, Method> lazyAutowiredMethod = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.autowiredProcessor = new AutowiredProcessor(context);
        this.doAutowiredBean(context);
    }

    @Override
    public void doAutowiredBean(Object bean) {
        bean = AopUtil.getSourceIfNecessary(bean);
        this.doAutowiredBeanField(bean.getClass(), bean);
        this.doAutowiredBeanMethod(bean.getClass(), bean);
    }

    @Override
    public void doAutowiredLazy() {
        for (Map.Entry<Object, Field> entry : this.lazyAutowiredField.entrySet()) {
            this.autowiredProcessor.doAutowired(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Object, Method> entry : this.lazyAutowiredMethod.entrySet()) {
            this.autowiredProcessor.doAutowired(entry.getKey(), entry.getValue());
        }
        this.lazyAutowiredField.clear();
        this.lazyAutowiredMethod.clear();
    }

    protected void doAutowiredBeanField(Class<?> clazz, Object bean) {
        List<Field> lazies = new ArrayList<>(4);
        List<Method> beanMethods = ReflectUtil.getMethods(clazz).stream().filter(e -> AnnotationUtil.hasAnnotation(e, Bean.class)).collect(Collectors.toList());
        for (Field field : ReflectUtil.getFieldMap(clazz).values()) {
            if(AnnotationUtil.hasAnnotation(field, Resource.class) || AnnotationUtil.hasAnnotation(field, Autowired.class)) {
                if(AnnotationUtil.hasAnnotation(field, Lazy.class)) {
                    this.lazyAutowiredField.put(bean, field);
                    continue;
                }
                ActualGeneric actualGeneric = ActualGeneric.from(clazz, field);
                if(beanMethods.stream().anyMatch(e -> actualGeneric.getSimpleActualType().isAssignableFrom(e.getReturnType()))) {
                    lazies.add(field);
                    continue;
                }
                this.autowiredProcessor.doAutowired(bean, field);
            }
        }
        lazies.forEach(e -> this.autowiredProcessor.doAutowired(bean, e));
    }

    protected void doAutowiredBeanMethod(Class<?> clazz, Object bean) {
        for (Method method : ReflectUtil.getMethods(clazz)) {
            if(AnnotationUtil.hasAnnotation(method, Resource.class) || AnnotationUtil.hasAnnotation(method, Autowired.class)) {
                if(AnnotationUtil.hasAnnotation(method, Lazy.class)) {
                    this.lazyAutowiredMethod.put(bean, method);
                    continue;
                }
                this.autowiredProcessor.doAutowired(bean, method);
            }
        }
    }
}
