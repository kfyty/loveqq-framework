package com.kfyty.core.autoconfig.beans;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Scope;
import com.kfyty.core.autoconfig.annotation.Value;
import com.kfyty.core.autoconfig.beans.autowired.AutowiredDescription;
import com.kfyty.core.autoconfig.beans.autowired.AutowiredProcessor;
import com.kfyty.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.core.autoconfig.env.PlaceholdersResolver;
import com.kfyty.core.generic.ActualGeneric;
import com.kfyty.core.support.Pair;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.BeanUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.LogUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.core.utils.ScopeUtil;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.kfyty.core.utils.AnnotationUtil.findAnnotation;
import static java.util.Optional.ofNullable;

/**
 * 描述: 简单的通用 bean 定义
 *
 * @author kfyty725
 * @date 2021/6/12 10:29
 * @email kfyty725@hotmail.com
 */
@Slf4j
@ToString
@EqualsAndHashCode
public class GenericBeanDefinition implements BeanDefinition {
    /**
     * 该 bean 注册的名称
     */
    protected String beanName;

    /**
     * 该 bean 注册的类型
     */
    protected Class<?> beanType;

    /**
     * bean 作用域
     */
    protected String scope;

    /**
     * 是否启用作用域代理
     */
    private boolean isScopeProxy;

    /**
     * 是否延迟初始化
     */
    protected boolean isLazyInit;

    /**
     * 是否代理懒加载的 bean
     */
    protected boolean isLazyProxy;

    /**
     * 是否是自动装配的候选者
     */
    protected boolean isAutowireCandidate;

    /**
     * 构造器
     */
    protected Constructor<?> constructor;

    /**
     * 默认构造器参数
     */
    protected List<Pair<Class<?>, Object>> defaultConstructorArgs;

    /**
     * 自动注入处理器，所有实例共享，以处理循环依赖
     */
    protected static AutowiredProcessor autowiredProcessor = null;

    public GenericBeanDefinition() {
        this.setScopeProxy(true);
        this.setLazyProxy(true);
        this.setAutowireCandidate(true);
    }

    public GenericBeanDefinition(Class<?> beanType) {
        this(BeanUtil.getBeanName(beanType), beanType);
    }

    public GenericBeanDefinition(String beanName, Class<?> beanType) {
        this(beanName, beanType, ScopeUtil.resolveScope(beanType));
    }

    public GenericBeanDefinition(String beanName, Class<?> beanType, Scope scope) {
        this(beanName, beanType, scope.value(), scope.scopeProxy());
    }

    public GenericBeanDefinition(String beanName, Class<?> beanType, String scope, boolean isScopeProxy) {
        this(beanName, beanType, scope, isScopeProxy, false, true);
    }

    public GenericBeanDefinition(String beanName, Class<?> beanType, String scope, boolean isScopeProxy, boolean isLazyInit, boolean isLazyProxy) {
        this();
        this.setBeanName(beanName);
        this.setBeanType(beanType);
        this.setScope(scope);
        this.setScopeProxy(isScopeProxy);
        this.setLazyInit(isLazyInit);
        this.setLazyProxy(isLazyProxy);
    }

    @Override
    public String getBeanName() {
        return this.beanName;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = Objects.requireNonNull(beanName);
    }

    @Override
    public Class<?> getBeanType() {
        return this.beanType;
    }

    @Override
    public void setBeanType(Class<?> beanType) {
        this.beanType = Objects.requireNonNull(beanType);
    }

    @Override
    public String getScope() {
        return this.scope;
    }

    @Override
    public boolean isScopeProxy() {
        return this.isScopeProxy;
    }

    @Override
    public void setScope(String scope) {
        this.scope = Objects.requireNonNull(scope);
    }

    @Override
    public void setScopeProxy(boolean isScopeProxy) {
        this.isScopeProxy = isScopeProxy;
    }

    @Override
    public boolean isLazyInit() {
        return this.isLazyInit;
    }

    @Override
    public boolean isLazyProxy() {
        return this.isLazyProxy;
    }

    @Override
    public void setLazyInit(boolean isLazyInit) {
        this.isLazyInit = isLazyInit;
    }

    @Override
    public void setLazyProxy(boolean isLazyProxy) {
        this.isLazyProxy = isLazyProxy;
    }

    @Override
    public boolean isSingleton() {
        return this.getScope().equals(SCOPE_SINGLETON);
    }

    @Override
    public boolean isFactoryBean() {
        return FactoryBean.class.isAssignableFrom(this.getBeanType());
    }

    @Override
    public boolean isAutowireCandidate() {
        return this.isAutowireCandidate;
    }

    @Override
    public void setAutowireCandidate(boolean autowireCandidate) {
        this.isAutowireCandidate = autowireCandidate;
    }

    @Override
    public BeanDefinition addConstructorArgs(Class<?> argType, Object arg) {
        if (this.defaultConstructorArgs == null) {
            this.defaultConstructorArgs = new LinkedList<>();
        }
        this.defaultConstructorArgs.add(new Pair<>(argType, arg));
        return this;
    }

    @Override
    public List<Pair<Class<?>, Object>> getConstructArgs() {
        return this.prepareConstructorArgs();
    }

    @Override
    public Class<?>[] getConstructArgTypes() {
        this.ensureConstructor();
        return this.constructor.getParameterTypes();
    }

    @Override
    public Object[] getConstructArgValues() {
        return this.getConstructArgs().stream().map(Pair::getValue).toArray();
    }

    @Override
    public Object createInstance(ApplicationContext context) {
        if (context.contains(this.getBeanName())) {
            return context.getBean(this.getBeanName());
        }
        this.ensureAutowiredProcessor(context);
        Object bean = ReflectUtil.newInstance(this.beanType, this.getConstructArgs());
        return LogUtil.logIfDebugEnabled(log, log ->  log.debug("instantiate bean: {}", bean), bean);
    }

    protected void ensureConstructor() {
        if (this.constructor == null) {
            Class<?>[] parameterClasses = CommonUtil.empty(this.defaultConstructorArgs) ? null : this.defaultConstructorArgs.stream().map(Pair::getKey).toArray(Class[]::new);
            this.constructor = ReflectUtil.searchSuitableConstructor(this.beanType, e -> Arrays.equals(e.getParameterTypes(), parameterClasses) || AnnotationUtil.hasAnnotation(e, Autowired.class));
        }
    }

    protected void ensureAutowiredProcessor(ApplicationContext context) {
        if (autowiredProcessor == null || autowiredProcessor.getContext() != context) {
            autowiredProcessor = new AutowiredProcessor(context);
        }
    }

    protected List<Pair<Class<?>, Object>> prepareConstructorArgs() {
        this.ensureConstructor();
        if (this.constructor.getParameterCount() == 0) {
            return Collections.emptyList();
        }
        Parameter[] parameters = this.constructor.getParameters();
        Autowired constructorAnnotation = findAnnotation(this.constructor, Autowired.class);
        List<Pair<Class<?>, Object>> constructorArgs = ofNullable(this.defaultConstructorArgs).map(LinkedList::new).orElseGet(LinkedList::new);
        for (int i = constructorArgs.size(); i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Value value = findAnnotation(parameter, Value.class);
            if (value != null) {
                constructorArgs.add(new Pair<>(parameter.getType(), this.resolvePlaceholderValue(value.value(), parameter.getParameterizedType())));
                continue;
            }
            Autowired autowired = ofNullable(findAnnotation(parameter, Autowired.class)).orElse(constructorAnnotation);
            Object resolveBean = autowiredProcessor.doResolveBean(ActualGeneric.from(this.beanType, parameter), AutowiredDescription.from(autowired), parameter.getType());
            constructorArgs.add(new Pair<>(parameter.getType(), resolveBean));
        }
        return constructorArgs;
    }

    protected Object resolvePlaceholderValue(String value, Type targetType) {
        ApplicationContext context = autowiredProcessor.getContext();
        PlaceholdersResolver placeholdersResolver = context.getBean(PlaceholdersResolver.class);
        GenericPropertiesContext propertiesContext = context.getBean(GenericPropertiesContext.class);
        return resolvePlaceholderValue(value, targetType, placeholdersResolver, propertiesContext);
    }

    public static Object resolvePlaceholderValue(String value, Type targetType, PlaceholdersResolver placeholdersResolver, GenericPropertiesContext propertyContext) {
        String tempKey = "__temp__" + UUID.randomUUID() + "__key__";
        try {
            String resolved = placeholdersResolver.resolvePlaceholders(value);
            propertyContext.setProperty(tempKey, resolved);
            return propertyContext.getProperty(tempKey, targetType);
        } finally {
            propertyContext.removeProperty(tempKey);
        }
    }
}
