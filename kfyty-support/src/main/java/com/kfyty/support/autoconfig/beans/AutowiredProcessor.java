package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Qualifier;
import com.kfyty.support.exception.BeansException;
import com.kfyty.support.generic.ActualGeneric;
import com.kfyty.support.generic.Generic;
import com.kfyty.support.generic.SimpleGeneric;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 描述: 自动注入处理器
 *
 * @author kfyty725
 * @date 2021/6/12 11:28
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class AutowiredProcessor {
    private final Set<String> resolving = new HashSet<>();

    private final ApplicationContext context;

    public AutowiredProcessor(ApplicationContext context) {
        this.context = context;
    }

    public void doAutowired(Class<?> clazz, Object bean, Field field) {
        if (ReflectUtil.getFieldValue(bean, field) != null) {
            return;
        }
        Autowired annotation = AnnotationUtil.findAnnotation(field, Autowired.class);
        ActualGeneric actualGeneric = ActualGeneric.from(clazz, field);
        String beanName = BeanUtil.getBeanName(actualGeneric.getSimpleActualType(), annotation);
        Object targetBean = this.doResolveBean(beanName, actualGeneric, annotation);
        if(AopUtil.isJdkProxy(targetBean) && field.getType().equals(AopUtil.getSourceIfNecessary(targetBean).getClass())) {
            targetBean = AopUtil.getSourceIfNecessary(targetBean);
        }
        ReflectUtil.setFieldValue(bean, field, targetBean);
        if(log.isDebugEnabled()) {
            log.debug("autowired bean: [{}] -> [{}] !", targetBean, bean);
        }
    }

    public void doAutowired(Object bean, Method method) {
        int index = 0;
        Object[] parameters = new Object[method.getParameterCount()];
        for (Parameter parameter : method.getParameters()) {
            String beanName = BeanUtil.getBeanName(parameter.getType(), AnnotationUtil.findAnnotation(parameter, Qualifier.class));
            Object targetBean = this.doResolveBean(beanName, ActualGeneric.from(parameter), AnnotationUtil.findAnnotation(parameter, Autowired.class));
            if(AopUtil.isJdkProxy(targetBean) && parameter.getType().equals(AopUtil.getSourceIfNecessary(targetBean).getClass())) {
                targetBean = AopUtil.getSourceIfNecessary(targetBean);
            }
            parameters[index++] = targetBean;
        }
        ReflectUtil.invokeMethod(bean, method, parameters);
        if(log.isDebugEnabled()) {
            log.debug("autowired bean: [{}] -> [{}] !", parameters, bean);
        }
    }

    /**
     * 解析 bean 依赖
     *
     * @param targetBeanName 目标 bean name，如果是泛型则忽略
     * @param returnType     目标 bean 类型
     * @return bean
     */
    public Object doResolveBean(String targetBeanName, ActualGeneric returnType, Autowired autowired) {
        if (resolving.contains(targetBeanName)) {
            throw new BeansException("bean circular dependency: " + targetBeanName);
        }
        this.prepareResolving(targetBeanName, returnType.getSimpleActualType(), returnType.isSimpleParameterizedType());
        Map<String, ?> beans = this.doGetBean(targetBeanName, returnType.getSimpleActualType(), returnType.isSimpleParameterizedType(), autowired);
        Object resolveBean = null;
        if(List.class.isAssignableFrom(returnType.getSourceType())) {
            resolveBean = new ArrayList<>(beans.values());
        }
        if(Set.class.isAssignableFrom(returnType.getSourceType())) {
            resolveBean = new HashSet<>(beans.values());
        }
        if(returnType.isMapGeneric()) {
            resolveBean = beans;
        }
        if(returnType.isSimpleArray()) {
            resolveBean = beans.values().toArray((Object[]) Array.newInstance(returnType.getSimpleActualType(), 0));
        }
        if(resolveBean == null) {
            resolveBean = beans.size() == 1 ? beans.values().iterator().next() : this.matchBeanIfNecessary(beans, targetBeanName, returnType);
        }
        this.removeResolving(targetBeanName, returnType.getSimpleActualType(), returnType.isSimpleParameterizedType());
        return resolveBean;
    }

    private void prepareResolving(String targetBeanName, Class<?> targetType, boolean isGeneric) {
        if(!isGeneric) {
            resolving.add(targetBeanName);
        } else {
            this.context.getBeanDefinitions(targetType).values().forEach(e -> resolving.add(e.getBeanName()));
        }
    }

    private void removeResolving(String targetBeanName, Class<?> targetType, boolean isGeneric) {
        if(!isGeneric) {
            resolving.remove(targetBeanName);
        } else {
            this.context.getBeanDefinitions(targetType).values().forEach(e -> resolving.remove(e.getBeanName()));
        }
    }

    private Map<String, ?> doGetBean(String targetBeanName, Class<?> targetType, boolean isGeneric, Autowired autowired) {
        Map<String, ?> beanOfType = this.context.getBeanOfType(targetType);
        Map<String, BeanDefinition> targetBeanDefinitions = this.context.getBeanDefinitions(targetType);
        if(beanOfType.isEmpty() || isGeneric && beanOfType.size() < targetBeanDefinitions.size() || !isGeneric && beanOfType.size() > 1 && !beanOfType.containsKey(targetBeanName)) {
            if(isGeneric) {
                targetBeanDefinitions.values().forEach(this.context::registerBean);
            } else {
                BeanDefinition beanDefinition = this.context.getBeanDefinition(targetBeanName, targetType);
                if(beanDefinition == null) {
                    throw new BeansException("resolve target bean failed, no bean definition found of name: " + targetBeanName);
                }
                this.context.registerBean(beanDefinition);
            }
        }
        beanOfType = this.context.getBeanOfType(targetType);
        if(autowiredRequired(autowired) && beanOfType.isEmpty() || autowiredRequired(autowired) && !isGeneric && beanOfType.size() > 1 && !beanOfType.containsKey(targetBeanName)) {
            throw new BeansException("resolve target bean failed, no bean found of name: " + targetBeanName);
        }
        return beanOfType;
    }

    private boolean autowiredRequired(Autowired autowired) {
        return autowired == null || autowired.required();
    }

    private Object matchBeanIfNecessary(Map<String, ?> beans, String beanName, ActualGeneric actualGeneric) {
        Object bean = beans.get(beanName);
        if(bean != null) {
            return bean;
        }
        List<Generic> targetGenerics = new ArrayList<>(actualGeneric.getGenericInfo().keySet());
        for (Object value : beans.values()) {
            SimpleGeneric generic = SimpleGeneric.from(value.getClass());
            if(generic.size() != targetGenerics.size()) {
                continue;
            }
            boolean matched = true;
            List<Generic> generics = new ArrayList<>(generic.getGenericInfo().keySet());
            for (int i = 0; i < generics.size(); i++) {
                Class<?> target = targetGenerics.get(i).get();
                Class<?> toBeMatched = generics.get(i).get();
                if(!Objects.equals(target, toBeMatched)) {
                    matched = false;
                    break;
                }
            }
            if(matched) {
                if(bean != null) {
                    throw new BeansException("resolve target bean failed, more than one generic bean found of name: " + beanName);
                }
                bean = value;
            }
        }
        return bean;
    }
}
