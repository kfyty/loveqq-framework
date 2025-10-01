package com.kfyty.loveqq.framework.core.autoconfig.beans.autowired;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.LaziedObject;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.beans.GenericBeanDefinition;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
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

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotation;
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
public class AutowiredProcessor {
    /**
     * 日志
     */
    private static final Logger log = LoggerFactory.getLogger(BeanFactory.class);

    /**
     * 由于作用域代理/懒加载代理等，会导致 {@link Bean} 注解的 bean name 发生变化，此时解析得到的 bean name 是代理后的 bean，返回会导致堆栈溢出，
     * 因此需要设置线程上下文 bean name，当解析与请求的不一致时，能够继续执行到 bean 方法，从而获取到真实的 bean
     */
    public static final ThreadLocal<String> CURRENT_CREATING_BEAN = new ThreadLocal<>();

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

    /**
     * 构造器
     *
     * @param context 上下文
     */
    public AutowiredProcessor(ApplicationContext context) {
        this(context, Objects.requireNonNull(context.getBean(AutowiredDescriptionResolver.class), "The bean doesn't exists of type: " + AutowiredDescriptionResolver.class));
    }

    /**
     * 构造器
     *
     * @param context  上下文
     * @param resolver 自动装配描述解析器
     */
    public AutowiredProcessor(ApplicationContext context, AutowiredDescriptionResolver resolver) {
        this.context = context;
        this.resolver = resolver;
        this.resolving = Collections.synchronizedSet(new LinkedHashSet<>());
    }

    /**
     * 自动装配一个实例属性
     *
     * @param bean  实例
     * @param field 属性
     * @return 自动装配的实例
     */
    public Object resolve(Object bean, Field field) {
        AutowiredDescription description = DefaultAutowiredDescriptionResolver.doResolve(field);
        if (description != null) {
            return this.doResolve(bean, field, description);
        }
        return null;
    }

    /**
     * 自动装配一个实例方法
     *
     * @param bean   实例
     * @param method 方法
     */
    public void resolve(Object bean, Method method) {
        AutowiredDescription description = DefaultAutowiredDescriptionResolver.doResolve(method);
        if (description != null) {
            this.doResolve(bean, method, description);
        }
    }

    /**
     * 自动装配一个实例属性
     *
     * @param bean        实例
     * @param field       属性
     * @param description 自动注入描述符解析器
     * @return 自动装配的实例
     */
    public Object doResolve(Object bean, Field field, AutowiredDescription description) {
        SimpleGeneric simpleGeneric = SimpleGeneric.from(bean.getClass(), field);
        Object targetBean = doResolve(simpleGeneric, description, field.getType());
        if (targetBean != null) {
            ReflectUtil.setFieldValue(bean, field, targetBean);
            LogUtil.logIfDebugEnabled(log, log -> log.debug("autowired bean: {} -> {}", targetBean, bean));
        }
        return targetBean;
    }

    /**
     * 自动装配一个实例方法
     *
     * @param bean        实例
     * @param method      方法
     * @param description 自动注入描述符解析器
     */
    public Object[] doResolve(Object bean, Method method, AutowiredDescription description) {
        return this.doResolve(bean, method, description, DefaultAutowiredDescriptionResolver::doResolve);
    }

    /**
     * 自动装配一个实例方法
     *
     * @param bean                                  实例
     * @param method                                方法
     * @param description                           自动注入描述符解析器
     * @param parameterAutowiredDescriptionResolver 方法参数转换为自动注入描述符
     */
    public Object[] doResolve(Object bean, Method method, AutowiredDescription description, Function<Parameter, AutowiredDescription> parameterAutowiredDescriptionResolver) {
        int index = 0;
        boolean shouldInvoke = false;
        Object[] parameters = new Object[method.getParameterCount()];
        for (Parameter parameter : method.getParameters()) {
            Value annotation = findAnnotation(parameter, Value.class);
            if (annotation != null) {
                parameters[index++] = GenericBeanDefinition.resolvePlaceholderValue(annotation.value(), annotation.bind(), parameter.getParameterizedType());
                shouldInvoke = true;
                continue;
            }
            SimpleGeneric simpleGeneric = SimpleGeneric.from(bean.getClass(), parameter);
            AutowiredDescription paramDescription = ofNullable(parameterAutowiredDescriptionResolver.apply(parameter)).orElse(description);
            Object targetBean = doResolve(simpleGeneric, paramDescription, parameter.getType());
            parameters[index++] = targetBean;
            shouldInvoke |= targetBean != null;
        }
        if (shouldInvoke) {
            ReflectUtil.invokeMethod(bean, method, parameters);
            return LogUtil.logIfDebugEnabled(log, log -> log.debug("autowired bean: {} -> {}", parameters, bean), parameters);
        }
        return parameters;
    }

    /**
     * 解析 bean 依赖
     * 仅解析自动装配的候选者
     *
     * @param simpleGeneric 可解析的泛型
     * @param description   自动注入描述
     * @param requiredType  请求的类型，若是泛型则是去除泛型后的原始类型
     * @return bean
     */
    public Object doResolve(SimpleGeneric simpleGeneric, AutowiredDescription description, Class<?> requiredType) {
        final String beanName = BeanUtil.getBeanName(simpleGeneric.getSimpleType(), description == null ? null : description.value());

        Object targetBean =
                simpleGeneric.isGeneric(LaziedObject.class)
                        ? Lazy.of(() -> doResolveBean(beanName, simpleGeneric, description), simpleGeneric, description, requiredType)
                        : doResolveBean(beanName, simpleGeneric, description);

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
        SimpleGeneric actualReturnType = preProcessGeneric(returnType, autowired);
        Map<String, Object> beans = doGetBean(targetBeanName, actualReturnType.getSimpleType(), actualReturnType, autowired);
        if (actualReturnType.isGeneric(List.class)) {
            resolveBean = new ArrayList<>(filterBeanGenericType(beans, actualReturnType).values());
        }
        if (actualReturnType.isGeneric(Set.class)) {
            resolveBean = new LinkedHashSet<>(filterBeanGenericType(beans, actualReturnType).values());
        }
        if (actualReturnType.isMapGeneric()) {
            resolveBean = filterBeanGenericType(beans, actualReturnType);
        }
        if (actualReturnType.isSimpleArray()) {
            resolveBean = CommonUtil.copyToArray(actualReturnType.getSimpleActualType(), filterBeanGenericType(beans, actualReturnType).values());
        }
        if (beans.isEmpty()) {
            return returnType.isGeneric(Optional.class) ? Optional.empty() : resolveBean;
        }
        if (resolveBean == null) {
            resolveBean = beans.size() == 1 ? beans.values().iterator().next() : matchBestBeanIfNecessary(beans, targetBeanName, actualReturnType, true);
        }
        if (returnType.isGeneric(Optional.class)) {
            return Optional.ofNullable(resolveBean);
        }
        return resolveBean;
    }

    /**
     * 泛型预处理
     * 对于 {@link Optional} 或者 {@link LaziedObject} 获取其嵌套泛型，以确保解析准确
     *
     * @param returnType 目标泛型
     * @param autowired  自动注入描述符
     * @return 预处理后的泛型
     */
    protected SimpleGeneric preProcessGeneric(SimpleGeneric returnType, AutowiredDescription autowired) {
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
            throw new BeansException("Bean circular dependency: \r\n" + this.buildCycleDependencyInfo());
        }
    }

    private synchronized void prepareResolving(String targetBeanName, Map<String, BeanDefinition> targetBeanDefinitions, boolean isGeneric) {
        if (!isGeneric) {
            this.checkResolving(targetBeanName);
            if (!this.context.containsReference(targetBeanName)) {
                this.resolving.add(targetBeanName);
            }
            return;
        }
        for (BeanDefinition beanDefinition : targetBeanDefinitions.values()) {
            this.checkResolving(beanDefinition.getBeanName());
            if (!this.context.containsReference(beanDefinition.getBeanName())) {
                this.resolving.add(beanDefinition.getBeanName());
            }
        }
    }

    private synchronized void removeResolving(String targetBeanName, Map<String, BeanDefinition> targetBeanDefinitions, boolean isGeneric) {
        if (!isGeneric) {
            this.resolving.remove(targetBeanName);
        } else {
            targetBeanDefinitions.values().forEach(e -> this.resolving.remove(e.getBeanName()));
        }
    }

    /**
     * 当使用同类型的 bean，再去创建一个同类型的 bean 时，移除当前 bean
     * eg：多数据源配置场景
     *
     * @param targetBeanDefinitions 全部 bean 定义
     */
    private void removeCreatingBeanIfNecessary(Map<String, BeanDefinition> targetBeanDefinitions) {
        if (targetBeanDefinitions.size() < 2) {
            return;
        }

        String creatingBean = CURRENT_CREATING_BEAN.get();

        if (creatingBean == null || !targetBeanDefinitions.containsKey(creatingBean) || this.context.contains(creatingBean)) {
            return;
        }

        if (this.resolving.contains(creatingBean)) {
            targetBeanDefinitions.remove(creatingBean);
        }
    }

    private Map<String, Object> doGetBean(String targetBeanName, Class<?> targetType, SimpleGeneric returnType, AutowiredDescription autowired) {
        final boolean isGeneric = returnType.isSimpleGeneric();
        Map<String, Object> beanOfType = new LinkedHashMap<>(2);
        Map<String, BeanDefinition> targetBeanDefinitions = new LinkedHashMap<>(2);
        if (!isGeneric && this.context.containsBeanDefinition(targetBeanName)) {
            Optional.of(this.context.getBeanDefinition(targetBeanName)).ifPresent(bd -> targetBeanDefinitions.put(bd.getBeanName(), bd));
        } else {
            targetBeanDefinitions.putAll(this.context.getBeanDefinitions(targetType));
            this.removeCreatingBeanIfNecessary(targetBeanDefinitions);
        }
        for (Iterator<Map.Entry<String, BeanDefinition>> i = targetBeanDefinitions.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, BeanDefinition> entry = i.next();
            if (!entry.getValue().isAutowireCandidate()) {
                i.remove();
                continue;
            }
            if (this.context.contains(entry.getKey())) {                                                    // 这里只能先直接判断，而不是获取判断非空
                beanOfType.put(entry.getKey(), this.context.getBean(entry.getKey()));
            } else if (isGeneric) {
                try {
                    this.prepareResolving(targetBeanName, targetBeanDefinitions, true);
                    this.context.registerBeanReference(entry.getValue());                                   // 泛型先注册 bean 引用，下一步再注册完整的 bean，避免循环依赖
                } finally {
                    this.removeResolving(targetBeanName, targetBeanDefinitions, true);
                }
            }
        }
        if (beanOfType.size() < targetBeanDefinitions.size()) {
            try {
                this.prepareResolving(targetBeanName, targetBeanDefinitions, isGeneric);
                if (isGeneric) {
                    beanOfType.clear();                                                                     // 先清空，保证按顺序注入
                    for (Map.Entry<String, BeanDefinition> entry : targetBeanDefinitions.entrySet()) {
                        beanOfType.put(entry.getKey(), this.context.registerBean(entry.getValue(), AutowiredDescription.isLazied(autowired)));
                    }
                } else {
                    BeanDefinition beanDefinition = targetBeanDefinitions.size() != 1 ? null : targetBeanDefinitions.values().iterator().next();
                    if (beanDefinition == null) {
                        this.matchBestBeanIfNecessary(targetBeanDefinitions, targetBeanName, returnType, false);
                        beanDefinition = targetBeanDefinitions.size() != 1 ? null : targetBeanDefinitions.values().iterator().next();
                    }
                    if (beanDefinition == null) {
                        beanDefinition = targetBeanDefinitions.values().stream().filter(BeanDefinition::isPrimary).findAny().orElse(targetBeanDefinitions.get(targetBeanName));
                    }
                    if (beanDefinition == null) {
                        if (!AutowiredDescription.isRequired(autowired)) {
                            return beanOfType;
                        }
                        throw new BeansException(CommonUtil.format("Resolve target bean failed, more than one bean definition of type {}, and there's not primary found", targetType));
                    }
                    beanOfType.put(beanDefinition.getBeanName(), this.context.registerBean(beanDefinition, AutowiredDescription.isLazied(autowired)));
                }
            } finally {
                this.removeResolving(targetBeanName, targetBeanDefinitions, isGeneric);
            }
        }
        if (AutowiredDescription.isRequired(autowired)) {
            // 不存在 bean 定义时会走到这里
            if (beanOfType.isEmpty()) {
                throw new BeansException("Resolve target bean failed, the bean doesn't exists of name: " + targetBeanName);
            }
            // 所有的 bean 已经被提前实例化时，会走到这里
            if (!isGeneric && beanOfType.size() > 1 && !beanOfType.containsKey(targetBeanName)) {
                Map<String, Object> primaryBeanOfType = beanOfType.entrySet().stream().filter(e -> this.context.getBeanDefinition(e.getKey()).isPrimary()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                if (primaryBeanOfType.size() > 1) {
                    throw new BeansException(CommonUtil.format("Resolve target bean failed, more than one bean of type {} found, and there's not primary found", targetType));
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
    private Map<String, Object> filterBeanGenericType(Map<String, Object> beans, SimpleGeneric returnType) {
        if (returnType.hasGeneric()) {
            Generic nestedGeneric = returnType.size() == 1 ? returnType.getFirst() : returnType.getSecond();
            QualifierGeneric matchTarget = returnType.getNested(nestedGeneric);
            if (matchTarget != null && matchTarget.hasGeneric()) {
                doFilterBeanGenericType(beans, matchTarget, false);
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
    private <T> T matchBestBeanIfNecessary(Map<String, T> beans, String beanName, SimpleGeneric returnType, boolean onlyOne) {
        T bean = beans.get(beanName);
        if (bean != null) {
            return bean;
        }
        if (returnType.hasGeneric()) {
            return doFilterBeanGenericType(beans, returnType, onlyOne);
        }
        if (onlyOne && beans.size() > 1) {
            throw new BeansException("Resolve target bean failed, more than one generic bean found of type: " + returnType.getResolveType());
        }
        return null;
    }

    private <T> T doFilterBeanGenericType(Map<String, T> beans, QualifierGeneric matchTarget, boolean onlyOne) {
        T bean = null;
        List<Generic> targetGenerics = new ArrayList<>(matchTarget.getGenericInfo().keySet());
        loop:
        for (Iterator<Map.Entry<String, T>> cursor = beans.entrySet().iterator(); cursor.hasNext(); ) {
            Map.Entry<String, T> entry = cursor.next();
            SimpleGeneric generic = buildGeneric(this.context.getBeanDefinition(entry.getKey()));
            if (generic.size() != targetGenerics.size()) {
                cursor.remove();
                continue;
            }
            List<Generic> generics = new ArrayList<>(generic.getGenericInfo().keySet());
            for (int i = 0; i < generics.size(); i++) {
                Class<?> targetClass = targetGenerics.get(i).get();
                if (targetClass != Object.class && !Objects.equals(targetClass, generics.get(i).get())) {
                    cursor.remove();
                    continue loop;
                }
            }
            if (onlyOne && bean != null) {
                throw new BeansException("Resolve target bean failed, more than one generic bean found of type: " + matchTarget.getResolveType());
            }
            bean = entry.getValue();
        }
        return bean;
    }

    private String buildCycleDependencyInfo() {
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
