package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Qualifier;
import com.kfyty.support.exception.BeansException;
import com.kfyty.support.jdbc.ReturnType;
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
        Autowired annotation = field.getAnnotation(Autowired.class);
        String beanName = BeanUtil.getBeanName(field.getType(), annotation);
        Object targetBean = this.doResolveBean(beanName, ReturnType.getReturnType(clazz, field), annotation);
        ReflectUtil.setFieldValue(bean, field, targetBean);
        if(log.isDebugEnabled()) {
            log.debug("autowired bean: [{}] -> [{}] !", targetBean, bean);
        }
    }

    public void doAutowired(Object bean, Method method) {
        int index = 0;
        Object[] parameters = new Object[method.getParameterCount()];
        for (Parameter parameter : method.getParameters()) {
            String beanName = BeanUtil.getBeanName(parameter.getType(), parameter.getAnnotation(Qualifier.class));
            parameters[index++] = this.doResolveBean(beanName, ReturnType.getReturnType(parameter), parameter.getAnnotation(Autowired.class));
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
    public Object doResolveBean(String targetBeanName, ReturnType<?, ?, ?> returnType, Autowired autowired) {
        if (resolving.contains(targetBeanName)) {
            throw new BeansException("bean circular dependency: " + targetBeanName);
        }
        this.prepareResolving(targetBeanName, returnType.getActualType(), returnType.isParameterizedType());
        Map<String, ?> beans = this.doGetBean(targetBeanName, returnType.getActualType(), returnType.isParameterizedType(), autowired);
        Object resolveBean = null;
        if(List.class.isAssignableFrom(returnType.getReturnType())) {
            resolveBean = new ArrayList<>(beans.values());
        }
        if(Set.class.isAssignableFrom(returnType.getReturnType())) {
            resolveBean = new HashSet<>(beans.values());
        }
        if(Map.class.isAssignableFrom(returnType.getReturnType())) {
            resolveBean = beans;
        }
        if(returnType.isArray()) {
            resolveBean = beans.values().toArray((Object[]) Array.newInstance(returnType.getActualType(), 0));
        }
        if(resolveBean == null) {
            resolveBean = beans.size() == 1 ? beans.values().iterator().next() : beans.get(targetBeanName);
        }
        this.removeResolving(targetBeanName, returnType.getActualType(), returnType.isParameterizedType());
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
}
