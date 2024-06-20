package com.kfyty.loveqq.framework.core.autoconfig.beans.autowired;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.LaziedObject;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.loveqq.framework.core.exception.BeansException;
import com.kfyty.loveqq.framework.core.generic.ActualGeneric;
import com.kfyty.loveqq.framework.core.generic.Generic;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.LogUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.DefaultAutowiredDescriptionResolver.doResolve;
import static com.kfyty.loveqq.framework.core.utils.AopUtil.getTargetClass;
import static com.kfyty.loveqq.framework.core.utils.AopUtil.isJdkProxy;
import static java.util.Optional.ofNullable;

/**
 * 描述: 自动注入处理器
 *
 * @author kfyty725
 * @date 2021/6/12 11:28
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class AutowiredProcessor {
    /**
     * 正在解析中的 bean name
     */
    private final Set<String> resolving;

    /**
     * 应用上下文
     */
    @Getter
    private final ApplicationContext context;

    /**
     * 自动注入描述符解析器
     */
    @Getter
    private final AutowiredDescriptionResolver resolver;

    public AutowiredProcessor(ApplicationContext context) {
        this(context, Objects.requireNonNull(context.getBean(AutowiredDescriptionResolver.class), "The bean doesn't exists of type: " + AutowiredDescriptionResolver.class));
    }

    public AutowiredProcessor(ApplicationContext context, AutowiredDescriptionResolver resolver) {
        this.context = context;
        this.resolver = resolver;
        this.resolving = Collections.synchronizedSet(new LinkedHashSet<>());
    }

    public Object doAutowired(Object bean, Field field) {
        AutowiredDescription description = doResolve(field);
        if (description != null) {
            return this.doAutowired(bean, field, description);
        }
        return null;
    }

    public void doAutowired(Object bean, Method method) {
        AutowiredDescription description = doResolve(method);
        if (description != null) {
            this.doAutowired(bean, method, description);
        }
    }

    public Object doAutowired(Object bean, Field field, AutowiredDescription description) {
        if (ReflectUtil.getFieldValue(bean, field) != null) {
            return null;
        }
        ActualGeneric actualGeneric = ActualGeneric.from(bean.getClass(), field);
        Object targetBean = this.doResolveBean(actualGeneric, description, field.getType());
        if (targetBean != null) {
            ReflectUtil.setFieldValue(bean, field, targetBean);
            LogUtil.logIfDebugEnabled(log, log -> log.debug("autowired bean: {} -> {}", targetBean, bean));
        }
        return targetBean;
    }

    public Object[] doAutowired(Object bean, Method method, AutowiredDescription description) {
        return this.doAutowired(bean, method, description, DefaultAutowiredDescriptionResolver::doResolve);
    }

    public Object[] doAutowired(Object bean, Method method, AutowiredDescription description, Function<Parameter, AutowiredDescription> parameterAutowiredDescriptionResolver) {
        int index = 0;
        Object[] parameters = new Object[method.getParameterCount()];
        for (Parameter parameter : method.getParameters()) {
            ActualGeneric actualGeneric = ActualGeneric.from(bean.getClass(), parameter);
            AutowiredDescription paramDescription = ofNullable(parameterAutowiredDescriptionResolver.apply(parameter)).orElse(description);
            Object targetBean = this.doResolveBean(actualGeneric, paramDescription, parameter.getType());
            parameters[index++] = targetBean;
        }
        ReflectUtil.invokeMethod(bean, method, parameters);
        return LogUtil.logIfDebugEnabled(log, log -> log.debug("autowired bean: {} -> {}", parameters, bean), parameters);
    }

    /**
     * 解析 bean 依赖
     * 仅解析自动装配的候选者
     *
     * @param actualGeneric 实际泛型
     * @param description   自动注入描述
     * @param requiredType  实际请求类型
     * @return bean
     */
    public Object doResolveBean(ActualGeneric actualGeneric, AutowiredDescription description, Class<?> requiredType) {
        String beanName = BeanUtil.getBeanName(actualGeneric.getSimpleActualType(), description == null ? null : description.value());
        Supplier<Object> targetBeanProvider = () -> this.doResolveBean(beanName, actualGeneric, description);
        Object targetBean = LaziedObject.class.isAssignableFrom(actualGeneric.getSourceType()) ? new Lazy<>(targetBeanProvider) : targetBeanProvider.get();
        if (targetBean != null && isJdkProxy(targetBean) && requiredType.equals(getTargetClass(targetBean))) {
            targetBean = AopUtil.getTarget(targetBean);
        }
        return targetBean;
    }

    /**
     * 解析 bean 依赖
     * 仅解析自动装配的候选者
     *
     * @param targetBeanName 目标 bean name，如果是泛型则忽略
     * @param returnType     目标 bean 类型
     * @return bean
     */
    public Object doResolveBean(String targetBeanName, ActualGeneric returnType, AutowiredDescription autowired) {
        Object resolveBean = null;
        Map<String, Object> beans = this.doGetBean(targetBeanName, returnType.getSimpleActualType(), returnType.isSimpleGeneric(), autowired);
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
            resolveBean = CommonUtil.copyToArray(returnType.getSimpleActualType(), beans.values());
        }
        if (beans.isEmpty()) {
            return resolveBean;
        }
        if (resolveBean == null) {
            resolveBean = beans.size() == 1 ? beans.values().iterator().next() : this.matchBeanIfNecessary(beans, targetBeanName, returnType);
        }
        return resolveBean;
    }

    private synchronized void checkResolving(String targetBeanName) {
        if (this.resolving.contains(targetBeanName)) {
            throw new BeansException("bean circular dependency: \r\n" + this.buildCircularDependency());
        }
    }

    private synchronized void prepareResolving(String targetBeanName, Class<?> targetType, boolean isGeneric) {
        if (!isGeneric) {
            this.checkResolving(targetBeanName);
            if (!this.context.containsReference(targetBeanName)) {
                this.resolving.add(targetBeanName);
            }
            return;
        }
        for (BeanDefinition beanDefinition : this.context.getBeanDefinitions(targetType).values()) {
            this.checkResolving(beanDefinition.getBeanName());
            if (!this.context.containsReference(beanDefinition.getBeanName())) {
                this.resolving.add(beanDefinition.getBeanName());
            }
        }
    }

    private synchronized void removeResolving(String targetBeanName, Class<?> targetType, boolean isGeneric) {
        if (!isGeneric) {
            this.resolving.remove(targetBeanName);
        } else {
            this.context.getBeanDefinitions(targetType).values().forEach(e -> this.resolving.remove(e.getBeanName()));
        }
    }

    private Map<String, Object> doGetBean(String targetBeanName, Class<?> targetType, boolean isGeneric, AutowiredDescription autowired) {
        Map<String, Object> beanOfType = new LinkedHashMap<>(2);
        Map<String, BeanDefinition> targetBeanDefinitions = new LinkedHashMap<>();
        if (this.context.containsBeanDefinition(targetBeanName)) {
            Optional.of(this.context.getBeanDefinition(targetBeanName)).ifPresent(bd -> targetBeanDefinitions.put(bd.getBeanName(), bd));
        } else {
            targetBeanDefinitions.putAll(this.context.getBeanDefinitions(targetType));
        }
        for (Iterator<Map.Entry<String, BeanDefinition>> i = targetBeanDefinitions.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, BeanDefinition> entry = i.next();
            if (!entry.getValue().isAutowireCandidate()) {
                i.remove();
                continue;
            }
            if (this.context.contains(entry.getKey())) {                                        // 这里只能先直接判断，而不是获取判断非空
                beanOfType.put(entry.getKey(), this.context.getBean(entry.getKey()));
            } else if (isGeneric) {
                try {
                    this.prepareResolving(targetBeanName, targetType, true);
                    this.context.registerBeanReference(entry.getValue());
                } finally {
                    this.removeResolving(targetBeanName, targetType, true);
                }
            }
        }
        if (beanOfType.size() < targetBeanDefinitions.size()) {
            try {
                this.prepareResolving(targetBeanName, targetType, isGeneric);
                if (isGeneric) {
                    for (Map.Entry<String, BeanDefinition> entry : targetBeanDefinitions.entrySet()) {
                        if (!beanOfType.containsKey(entry.getKey())) {
                            beanOfType.put(entry.getKey(), this.context.registerBean(entry.getValue(), AutowiredDescription.isLazied(autowired)));
                        }
                    }
                } else {
                    BeanDefinition beanDefinition = targetBeanDefinitions.size() != 1 ? null : targetBeanDefinitions.values().iterator().next();
                    if (beanDefinition == null) {
                        beanDefinition = targetBeanDefinitions.values().stream().filter(BeanDefinition::isPrimary).findAny().orElse(targetBeanDefinitions.get(targetBeanName));
                    }
                    if (beanDefinition == null) {
                        if (!AutowiredDescription.isRequired(autowired)) {
                            return beanOfType;
                        }
                        throw new BeansException(CommonUtil.format("resolve target bean failed, more than one bean definition of type {}, and no primary found", targetType));
                    }
                    beanOfType.put(beanDefinition.getBeanName(), this.context.registerBean(beanDefinition, AutowiredDescription.isLazied(autowired)));
                }
            } finally {
                this.removeResolving(targetBeanName, targetType, isGeneric);
            }
        }
        if (AutowiredDescription.isRequired(autowired) && beanOfType.isEmpty()) {
            throw new BeansException("resolve target bean failed, the bean doesn't exists of name: " + targetBeanName);
        }
        if (AutowiredDescription.isRequired(autowired) && !isGeneric && beanOfType.size() > 1 && !beanOfType.containsKey(targetBeanName)) {
            Map<String, Object> primaryBeanOfType = beanOfType.entrySet().stream().filter(e -> this.context.getBeanDefinition(e.getKey()).isPrimary()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            if (primaryBeanOfType.size() > 1) {
                throw new BeansException(CommonUtil.format("resolve target bean failed, more than one bean of type {} found, and no primary found", targetType));
            }
            return primaryBeanOfType;
        }
        return beanOfType;
    }

    private Object matchBeanIfNecessary(Map<String, ?> beans, String beanName, ActualGeneric actualGeneric) {
        Object bean = beans.get(beanName);
        if (bean != null) {
            return bean;
        }
        List<Generic> targetGenerics = new ArrayList<>(actualGeneric.getGenericInfo().keySet());
        loop:
        for (Map.Entry<String, ?> entry : beans.entrySet()) {
            Object value = entry.getValue();
            SimpleGeneric generic = buildGeneric(this.context.getBeanDefinition(entry.getKey()));
            if (generic.size() != targetGenerics.size()) {
                continue;
            }
            List<Generic> generics = new ArrayList<>(generic.getGenericInfo().keySet());
            for (int i = 0; i < generics.size(); i++) {
                if (!Objects.equals(targetGenerics.get(i).get(), generics.get(i).get())) {
                    continue loop;
                }
            }
            bean = value;
        }
        if (bean == null) {
            throw new BeansException("resolve target bean failed, more than one generic bean found of name: " + beanName);
        }
        return bean;
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

    public static SimpleGeneric buildGeneric(BeanDefinition beanDefinition) {
        if (beanDefinition instanceof MethodBeanDefinition) {
            return SimpleGeneric.from(((MethodBeanDefinition) beanDefinition).getBeanMethod());
        }
        return SimpleGeneric.from(beanDefinition.getBeanType());
    }
}
