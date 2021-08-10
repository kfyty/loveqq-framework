package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.exception.BeansException;
import com.kfyty.support.generic.ActualGeneric;
import com.kfyty.support.generic.Generic;
import com.kfyty.support.generic.SimpleGeneric;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 描述: 自动注入处理器
 *
 * @author kfyty725
 * @date 2021/6/12 11:28
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class AutowiredProcessor {
    private final Set<String> resolving;
    private final ApplicationContext context;

    public AutowiredProcessor(ApplicationContext context) {
        this.context = context;
        this.resolving = new LinkedHashSet<>();
    }

    public ApplicationContext getContext() {
        return context;
    }

    public void doAutowired(Object bean, Field field) {
        if (ReflectUtil.getFieldValue(bean, field) != null) {
            return;
        }
        Autowired annotation = this.findAutowiredAnnotation(field);
        ActualGeneric actualGeneric = ActualGeneric.from(bean.getClass(), field);
        String beanName = BeanUtil.getBeanName(actualGeneric.getSimpleActualType(), annotation);
        Object targetBean = this.doResolveBean(beanName, actualGeneric, annotation);
        if (targetBean != null && AopUtil.isJdkProxy(targetBean) && field.getType().equals(AopUtil.getSourceClass(targetBean))) {
            targetBean = AopUtil.getSourceTarget(targetBean);
        }
        if (targetBean != null) {
            ReflectUtil.setFieldValue(bean, field, targetBean);
            if (log.isDebugEnabled()) {
                log.debug("autowired bean: {} -> {} !", targetBean, bean);
            }
        }
    }

    public void doAutowired(Object bean, Method method) {
        int index = 0;
        Object[] parameters = new Object[method.getParameterCount()];
        for (Parameter parameter : method.getParameters()) {
            Autowired autowired = AnnotationUtil.findAnnotation(parameter, Autowired.class);
            Object targetBean = this.doResolveBean(BeanUtil.getBeanName(parameter), ActualGeneric.from(bean.getClass(), parameter), autowired != null ? autowired : this.findAutowiredAnnotation(method));
            if (targetBean != null && AopUtil.isJdkProxy(targetBean) && parameter.getType().equals(AopUtil.getSourceClass(targetBean))) {
                targetBean = AopUtil.getSourceTarget(targetBean);
            }
            parameters[index++] = targetBean;
        }
        ReflectUtil.invokeMethod(bean, method, parameters);
        if (log.isDebugEnabled()) {
            log.debug("autowired bean: {} -> {} !", parameters, bean);
        }
    }

    /**
     * 解析 bean 依赖
     * 仅解析自动装配的候选者
     *
     * @param targetBeanName 目标 bean name，如果是泛型则忽略
     * @param returnType     目标 bean 类型
     * @return bean
     */
    public Object doResolveBean(String targetBeanName, ActualGeneric returnType, Autowired autowired) {
        Object resolveBean = null;
        Map<String, Object> beans = this.doGetBean(targetBeanName, returnType.getSimpleActualType(), returnType.isSimpleParameterizedType(), autowired);
        if (List.class.isAssignableFrom(returnType.getSourceType())) {
            resolveBean = new ArrayList<>(beans.values());
        }
        if (Set.class.isAssignableFrom(returnType.getSourceType())) {
            resolveBean = new HashSet<>(beans.values());
        }
        if (returnType.isMapGeneric()) {
            resolveBean = beans;
        }
        if (returnType.isSimpleArray()) {
            resolveBean = beans.values().toArray((Object[]) Array.newInstance(returnType.getSimpleActualType(), 0));
        }
        if (resolveBean == null) {
            resolveBean = beans.size() == 1 ? beans.values().iterator().next() : this.matchBeanIfNecessary(beans, targetBeanName, returnType);
        }
        return resolveBean;
    }

    private Autowired findAutowiredAnnotation(Field field) {
        Autowired autowired = AnnotationUtil.findAnnotation(field, Autowired.class);
        if (autowired != null) {
            return autowired;
        }
        return this.createAutowiredAnnotation(AnnotationUtil.findAnnotation(field, Resource.class), field);
    }

    private Autowired findAutowiredAnnotation(Method method) {
        Autowired autowired = AnnotationUtil.findAnnotation(method, Autowired.class);
        if (autowired != null) {
            return autowired;
        }
        return this.createAutowiredAnnotation(AnnotationUtil.findAnnotation(method, Resource.class), null);
    }

    private void checkResolving(String targetBeanName) {
        if (resolving.contains(targetBeanName)) {
            throw new BeansException("bean circular dependency: \r\n" + this.buildCircularDependency());
        }
    }

    private void prepareResolving(String targetBeanName, Class<?> targetType, boolean isGeneric) {
        if (!isGeneric) {
            this.checkResolving(targetBeanName);
            if (!this.context.containsReference(targetBeanName)) {
                resolving.add(targetBeanName);
            }
            return;
        }
        for (BeanDefinition beanDefinition : this.context.getBeanDefinitions(targetType).values()) {
            this.checkResolving(beanDefinition.getBeanName());
            if (!this.context.containsReference(beanDefinition.getBeanName())) {
                resolving.add(beanDefinition.getBeanName());
            }
        }
    }

    private void removeResolving(String targetBeanName, Class<?> targetType, boolean isGeneric) {
        if (!isGeneric) {
            resolving.remove(targetBeanName);
        } else {
            this.context.getBeanDefinitions(targetType).values().forEach(e -> resolving.remove(e.getBeanName()));
        }
    }

    private Map<String, Object> doGetBean(String targetBeanName, Class<?> targetType, boolean isGeneric, Autowired autowired) {
        Map<String, Object> beanOfType = new HashMap<>(2);
        Map<String, BeanDefinition> targetBeanDefinitions = this.context.getBeanDefinitions(targetType).entrySet().stream().filter(e -> e.getValue().isAutowireCandidate()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        for (Map.Entry<String, BeanDefinition> entry : targetBeanDefinitions.entrySet()) {
            if (this.context.contains(entry.getKey())) {
                beanOfType.put(entry.getKey(), this.context.getBean(entry.getKey()));
            } else if (isGeneric) {
                this.prepareResolving(targetBeanName, targetType, true);
                this.context.registerBeanReference(entry.getValue());
                this.removeResolving(targetBeanName, targetType, true);
            }
        }
        if (beanOfType.size() < targetBeanDefinitions.size()) {
            try {
                this.prepareResolving(targetBeanName, targetType, isGeneric);
                if (isGeneric) {
                    for (Map.Entry<String, BeanDefinition> entry : targetBeanDefinitions.entrySet()) {
                        if (!beanOfType.containsKey(entry.getKey())) {
                            beanOfType.put(entry.getKey(), this.context.registerBean(entry.getValue()));
                        }
                    }
                } else {
                    BeanDefinition beanDefinition = targetBeanDefinitions.size() == 1 ? targetBeanDefinitions.values().iterator().next() : targetBeanDefinitions.get(targetBeanName);
                    if (beanDefinition == null) {
                        if (!autowiredRequired(autowired)) {
                            return beanOfType;
                        }
                        throw new BeansException(CommonUtil.format("resolve target bean failed, more than one bean definition of type {}, but no bean definition found of name: {}", targetType, targetBeanName));
                    }
                    beanOfType.put(beanDefinition.getBeanName(), this.context.registerBean(beanDefinition));
                }
            } finally {
                this.removeResolving(targetBeanName, targetType, isGeneric);
            }
        }
        if (autowiredRequired(autowired) && beanOfType.isEmpty() || autowiredRequired(autowired) && !isGeneric && beanOfType.size() > 1 && !beanOfType.containsKey(targetBeanName)) {
            throw new BeansException("resolve target bean failed, no bean found of name: " + targetBeanName);
        }
        return beanOfType;
    }

    private boolean autowiredRequired(Autowired autowired) {
        return autowired == null || autowired.required();
    }

    private Object matchBeanIfNecessary(Map<String, ?> beans, String beanName, ActualGeneric actualGeneric) {
        Object bean = beans.get(beanName);
        if (bean != null) {
            return bean;
        }
        List<Generic> targetGenerics = new ArrayList<>(actualGeneric.getGenericInfo().keySet());
        for (Object value : beans.values()) {
            SimpleGeneric generic = SimpleGeneric.from(AopUtil.getSourceClass(value));
            if (generic.size() != targetGenerics.size()) {
                continue;
            }
            boolean matched = true;
            List<Generic> generics = new ArrayList<>(generic.getGenericInfo().keySet());
            for (int i = 0; i < generics.size(); i++) {
                if (!Objects.equals(targetGenerics.get(i).get(), generics.get(i).get())) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                if (bean != null) {
                    throw new BeansException("resolve target bean failed, more than one generic bean found of name: " + beanName);
                }
                bean = value;
            }
        }
        return bean;
    }

    private Autowired createAutowiredAnnotation(Resource resource, Field field) {
        return resource == null ? null : new Autowired() {

            @Override
            public boolean required() {
                return true;
            }

            @Override
            public String value() {
                return field == null || CommonUtil.notEmpty(resource.name()) ? resource.name() : field.getName();
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Autowired.class;
            }
        };
    }

    private String buildCircularDependency() {
        StringBuilder builder = new StringBuilder("┌─────┐\r\n");
        Object[] beanNames = this.resolving.toArray();
        for (int i = 0; i < beanNames.length; i++) {
            builder.append(beanNames[i]).append(" -> ").append(this.context.getBeanDefinition(beanNames[i].toString())).append("\r\n");
            if (i < beanNames.length - 1) {
                builder.append("↑     ↓\r\n");
            }
        }
        return builder.append("└─────┘").toString();
    }
}
