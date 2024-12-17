package com.kfyty.loveqq.framework.core.autoconfig.beans.autowired;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.LaziedObject;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.exception.BeansException;
import com.kfyty.loveqq.framework.core.generic.Generic;
import com.kfyty.loveqq.framework.core.generic.QualifierGeneric;
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
import java.util.stream.Collectors;

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
        AutowiredDescription description = DefaultAutowiredDescriptionResolver.doResolve(field);
        if (description != null) {
            return this.doAutowired(bean, field, description);
        }
        return null;
    }

    public void doAutowired(Object bean, Method method) {
        AutowiredDescription description = DefaultAutowiredDescriptionResolver.doResolve(method);
        if (description != null) {
            this.doAutowired(bean, method, description);
        }
    }

    public Object doAutowired(Object bean, Field field, AutowiredDescription description) {
        if (ReflectUtil.getFieldValue(bean, field) != null) {
            return null;
        }
        SimpleGeneric simpleGeneric = SimpleGeneric.from(bean.getClass(), field);
        Object targetBean = this.doResolveBean(simpleGeneric, description, field.getType());
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
            SimpleGeneric simpleGeneric = SimpleGeneric.from(bean.getClass(), parameter);
            AutowiredDescription paramDescription = ofNullable(parameterAutowiredDescriptionResolver.apply(parameter)).orElse(description);
            Object targetBean = this.doResolveBean(simpleGeneric, paramDescription, parameter.getType());
            parameters[index++] = targetBean;
        }
        ReflectUtil.invokeMethod(bean, method, parameters);
        return LogUtil.logIfDebugEnabled(log, log -> log.debug("autowired bean: {} -> {}", parameters, bean), parameters);
    }

    /**
     * 解析 bean 依赖
     * 仅解析自动装配的候选者
     *
     * @param simpleGeneric 实际泛型
     * @param description   自动注入描述
     * @param requiredType  实际请求类型
     * @return bean
     */
    public Object doResolveBean(SimpleGeneric simpleGeneric, AutowiredDescription description, Class<?> requiredType) {
        String beanName = BeanUtil.getBeanName(simpleGeneric.getSimpleType(), description == null ? null : description.value());
        Object targetBean = simpleGeneric.isGeneric(LaziedObject.class)
                ? new Lazy<>(() -> this.doResolveBean(beanName, simpleGeneric, description))
                : this.doResolveBean(beanName, simpleGeneric, description);
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
    public Object doResolveBean(String targetBeanName, SimpleGeneric returnType, AutowiredDescription autowired) {
        Object resolveBean = null;
        SimpleGeneric actualReturnType = this.preProcessGeneric(returnType, autowired);
        Map<String, Object> beans = this.doGetBean(targetBeanName, actualReturnType.getSimpleType(), actualReturnType, autowired);
        if (actualReturnType.isGeneric(List.class)) {
            resolveBean = new ArrayList<>(this.filterMapBeanIfNecessary(beans, actualReturnType).values());
        }
        if (actualReturnType.isGeneric(Set.class)) {
            resolveBean = new HashSet<>(this.filterMapBeanIfNecessary(beans, actualReturnType).values());
        }
        if (actualReturnType.isMapGeneric()) {
            resolveBean = this.filterMapBeanIfNecessary(beans, actualReturnType);
        }
        if (actualReturnType.isSimpleArray()) {
            resolveBean = CommonUtil.copyToArray(actualReturnType.getSimpleActualType(), this.filterMapBeanIfNecessary(beans, actualReturnType).values());
        }
        if (beans.isEmpty()) {
            return returnType.isGeneric(Optional.class) ? Optional.empty() : resolveBean;
        }
        if (resolveBean == null) {
            resolveBean = beans.size() == 1 ? beans.values().iterator().next() : this.matchBeanIfNecessary(beans, targetBeanName, actualReturnType, true);
        }
        if (returnType.isGeneric(Optional.class)) {
            return Optional.ofNullable(resolveBean);
        }
        return resolveBean;
    }

    private SimpleGeneric preProcessGeneric(SimpleGeneric returnType, AutowiredDescription autowired) {
        if (returnType.isGeneric(Optional.class)) {
            autowired.markRequired(false);
            SimpleGeneric nested = (SimpleGeneric) returnType.getNestedGeneric();
            return nested == null ? returnType : nested;
        }
        if (returnType.isGeneric(LaziedObject.class)) {
            SimpleGeneric nested = (SimpleGeneric) returnType.getNestedGeneric();
            return nested == null ? returnType : nested;
        }
        return returnType;
    }

    private synchronized void checkResolving(String targetBeanName) {
        if (this.resolving.contains(targetBeanName)) {
            throw new BeansException("Bean circular dependency: \r\n" + this.buildCircularDependency());
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

    private Map<String, Object> doGetBean(String targetBeanName, Class<?> targetType, SimpleGeneric returnType, AutowiredDescription autowired) {
        boolean isGeneric = returnType.isSimpleGeneric();
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
                    this.context.registerBeanReference(entry.getValue());                       // 泛型先注册 bean 引用，下一步再注册完整的 bean，避免循环依赖
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
                        this.matchBeanIfNecessary(targetBeanDefinitions, targetBeanName, returnType, false);
                        beanDefinition = targetBeanDefinitions.size() != 1 ? null : targetBeanDefinitions.values().iterator().next();
                    }
                    if (beanDefinition == null) {
                        beanDefinition = targetBeanDefinitions.values().stream().filter(BeanDefinition::isPrimary).findAny().orElse(targetBeanDefinitions.get(targetBeanName));
                    }
                    if (beanDefinition == null) {
                        if (!AutowiredDescription.isRequired(autowired)) {
                            return beanOfType;
                        }
                        throw new BeansException(CommonUtil.format("Resolve target bean failed, more than one bean definition of type {}, and no primary found", targetType));
                    }
                    beanOfType.put(beanDefinition.getBeanName(), this.context.registerBean(beanDefinition, AutowiredDescription.isLazied(autowired)));
                }
            } finally {
                this.removeResolving(targetBeanName, targetType, isGeneric);
            }
        }
        if (AutowiredDescription.isRequired(autowired)) {
            if (beanOfType.isEmpty()) {
                throw new BeansException("Resolve target bean failed, the bean doesn't exists of name: " + targetBeanName);
            }
            if (!isGeneric && beanOfType.size() > 1 && !beanOfType.containsKey(targetBeanName)) {
                Map<String, Object> primaryBeanOfType = beanOfType.entrySet().stream().filter(e -> this.context.getBeanDefinition(e.getKey()).isPrimary()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                if (primaryBeanOfType.size() > 1) {
                    throw new BeansException(CommonUtil.format("Resolve target bean failed, more than one bean of type {} found, and no primary found", targetType));
                }
                return primaryBeanOfType.isEmpty() ? beanOfType : primaryBeanOfType;
            }
        }
        return beanOfType;
    }

    /**
     * 过滤嵌套的泛型，eg: Map<String, Bean<K, V>>，将会匹配 K、V 的泛型
     *
     * @param beans      同一类型但泛型不同的 bean
     * @param returnType 要注入的泛型
     * @return 过滤后的 bean
     */
    private Map<String, Object> filterMapBeanIfNecessary(Map<String, Object> beans, SimpleGeneric returnType) {
        if (!returnType.hasGeneric()) {
            return beans;
        }
        Generic nestedGeneric = returnType.size() == 1 ? returnType.getFirst() : returnType.getSecond();
        QualifierGeneric valueGeneric = returnType.getNested(nestedGeneric);
        if (valueGeneric == null || !valueGeneric.hasGeneric()) {
            return beans;
        }
        List<Generic> targetGenerics = new ArrayList<>(valueGeneric.getGenericInfo().keySet());
        loop:
        for (Iterator<Map.Entry<String, Object>> i = beans.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, Object> entry = i.next();
            SimpleGeneric generic = buildGeneric(this.context.getBeanDefinition(entry.getKey()));
            if (generic.size() != targetGenerics.size()) {
                i.remove();
                continue;
            }
            List<Generic> generics = new ArrayList<>(generic.getGenericInfo().keySet());
            for (int j = 0; j < generics.size(); j++) {
                Class<?> targetClass = targetGenerics.get(j).get();
                if (targetClass != Object.class && !Objects.equals(targetClass, generics.get(j).get())) {
                    i.remove();
                    continue loop;
                }
            }
        }
        return beans;
    }

    /**
     * 匹配最佳的 bean，eg: Bean<K1, V1>、Bean<K2, V2>，将根据泛型匹配最佳的 bean
     *
     * @param beans      同一类型但泛型不同的 bean
     * @param beanName   bean 名称
     * @param returnType 要注入的泛型
     * @param onlyOne    是否仅匹配一个。true 时若匹配多个将抛出异常；false 时将返回最后一个匹配的，并将 beans 中不匹配的移除掉
     * @return 匹配的最佳 bean
     */
    private <T> T matchBeanIfNecessary(Map<String, T> beans, String beanName, SimpleGeneric returnType, boolean onlyOne) {
        T bean = beans.get(beanName);
        if (bean != null || !returnType.hasGeneric()) {
            return bean;
        }
        List<Generic> targetGenerics = new ArrayList<>(returnType.getGenericInfo().keySet());
        loop:
        for (Iterator<Map.Entry<String, T>> cursor = beans.entrySet().iterator(); cursor.hasNext(); ) {
            Map.Entry<String, T> entry = cursor.next();
            T value = entry.getValue();
            SimpleGeneric generic = buildGeneric(this.context.getBeanDefinition(entry.getKey()));
            if (generic.size() != targetGenerics.size()) {
                cursor.remove();
                continue;
            }
            List<Generic> generics = new ArrayList<>(generic.getGenericInfo().keySet());
            for (int i = 0; i < generics.size(); i++) {
                if (!Objects.equals(targetGenerics.get(i).get(), generics.get(i).get())) {
                    cursor.remove();
                    continue loop;
                }
            }
            if (onlyOne && bean != null) {
                throw new BeansException("Resolve target bean failed, more than one generic bean found of name: " + beanName);
            }
            bean = value;
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
        Method beanMethod = beanDefinition.getBeanMethod();
        if (beanMethod != null) {
            return SimpleGeneric.from(beanMethod);
        }
        return SimpleGeneric.from(beanDefinition.getBeanType());
    }
}
