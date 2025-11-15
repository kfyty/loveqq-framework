package com.kfyty.loveqq.framework.core.autoconfig.beans;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPreProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Lazy;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Primary;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Scope;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.AutowiredDescription;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.AutowiredProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.DelegatedAutowiredDescriptionResolver;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.property.PropertyValue;
import com.kfyty.loveqq.framework.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.loveqq.framework.core.autoconfig.env.PlaceholdersResolver;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.LazyUtil;
import com.kfyty.loveqq.framework.core.utils.LogUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.core.utils.ScopeUtil;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.kfyty.loveqq.framework.core.utils.ConverterUtil.convert;
import static java.util.Optional.ofNullable;

/**
 * 描述: 简单的通用 bean 定义
 *
 * @author kfyty725
 * @date 2021/6/12 10:29
 * @email kfyty725@hotmail.com
 */
@Slf4j
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
    protected boolean isScopeProxy;

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
     * 属性值
     */
    protected List<PropertyValue> propertyValues;

    /**
     * bean 的初始化方法
     */
    protected String initMethod;

    /**
     * bean 的销毁方法
     */
    protected String destroyMethod;

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
        this(beanName, beanType, ScopeUtil.resolveScope(beanType), LazyUtil.resolveLazy(beanType));
    }

    public GenericBeanDefinition(String beanName, Class<?> beanType, Scope scope, Lazy lazy) {
        this(beanName, beanType, scope.value(), scope.scopeProxy(), lazy != null, lazy != null && lazy.lazyProxy());
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
    public boolean isApplicationSingleton() {
        return this.getScope().equals(SCOPE_APPLICATION);
    }

    @Override
    public boolean isSingleton() {
        String scope = this.getScope();
        return scope.equals(SCOPE_SINGLETON) || scope.equals(SCOPE_APPLICATION);
    }

    @Override
    public boolean isPrimary() {
        return AnnotationUtil.hasAnnotation(this.getBeanType(), Primary.class);
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
    public boolean isMethodBean() {
        return this.getBeanMethod() != null;
    }

    @Override
    public Method getBeanMethod() {
        return null;
    }

    @Override
    public Method getInitMethod(Object bean) {
        if (this.initMethod == null) {
            return null;
        }
        return ReflectUtil.getMethod(bean.getClass(), this.initMethod);
    }

    @Override
    public Method getDestroyMethod(Object bean) {
        if (this.destroyMethod == null) {
            return null;
        }
        return ReflectUtil.getMethod(bean.getClass(), this.destroyMethod);
    }

    @Override
    public void setInitMethod(String initMethod) {
        this.initMethod = initMethod;
    }

    @Override
    public void setDestroyMethod(String destroyMethod) {
        this.destroyMethod = destroyMethod;
    }

    @Override
    public String getInitMethod() {
        return this.initMethod;
    }

    @Override
    public String getDestroyMethod() {
        return this.destroyMethod;
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
    public BeanDefinition addPropertyValue(PropertyValue propertyValue) {
        if (this.propertyValues == null) {
            this.propertyValues = new LinkedList<>();
        }
        this.propertyValues.add(propertyValue);
        return this;
    }

    @Override
    public List<Pair<Class<?>, Object>> getConstructArgs() {
        return this.prepareConstructorArgs();
    }

    @Override
    public List<Pair<Class<?>, Object>> getDefaultConstructArgs() {
        return this.defaultConstructorArgs;
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
    public List<PropertyValue> getPropertyValues() {
        return this.propertyValues == null ? Collections.emptyList() : this.propertyValues;
    }

    @Override
    public Object createInstance(ApplicationContext context) {
        if (context.contains(this.getBeanName())) {
            return context.getBean(this.getBeanName());
        }
        this.ensureAutowiredProcessor(context);
        Object bean = ReflectUtil.newInstance(this.beanType, this.getConstructArgs());
        return LogUtil.logIfDebugEnabled(log, log -> log.debug("instantiate bean: {}", bean), bean);
    }

    @Override
    public String toString() {
        return "BeanDefinition[beanName=" + beanName +
                ", beanType=" + beanType +
                ", scope=" + scope +
                ", isScopeProxy=" + isScopeProxy +
                ", isLazy=" + isLazyInit +
                ", isLazyProxy=" + isLazyProxy +
                ", isAutowireCandidate=" + isAutowireCandidate +
                ", Constructor=" + constructor + "]";
    }

    protected void ensureConstructor() {
        if (this.constructor == null) {
            Class<?>[] parameterClasses = CommonUtil.empty(this.defaultConstructorArgs) ? null : this.defaultConstructorArgs.stream().map(Pair::getKey).toArray(Class[]::new);
            this.constructor = ReflectUtil.searchSuitableConstructor(this.beanType, e -> Arrays.equals(e.getParameterTypes(), parameterClasses) || AnnotationUtil.hasAnnotation(e, Autowired.class));
        }
    }

    protected void ensureAutowiredProcessor(ApplicationContext context) {
        if (autowiredProcessor == null || autowiredProcessor.getContext() != context) {
            if (BeanFactoryPreProcessor.class.isAssignableFrom(this.beanType)) {
                autowiredProcessor = new AutowiredProcessor(context, new DelegatedAutowiredDescriptionResolver());
            } else {
                autowiredProcessor = new AutowiredProcessor(context);
            }
        }
    }

    protected List<Pair<Class<?>, Object>> prepareConstructorArgs() {
        this.ensureConstructor();
        if (this.constructor.getParameterCount() == 0) {
            return Collections.emptyList();
        }
        Parameter[] parameters = this.constructor.getParameters();
        AutowiredDescription constructorDescription = autowiredProcessor.getResolver().resolve(this.constructor);
        List<Pair<Class<?>, Object>> constructorArgs = ofNullable(this.defaultConstructorArgs).map(LinkedList::new).orElseGet(LinkedList::new);
        for (int i = constructorArgs.size(); i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Value value = AnnotationUtil.findAnnotation(parameter, Value.class);
            if (value != null) {
                constructorArgs.add(new Pair<>(parameter.getType(), resolvePlaceholderValue(value.value(), value.bind(), parameter.getParameterizedType())));
                continue;
            }
            AutowiredDescription description = ofNullable(autowiredProcessor.getResolver().resolve(parameter)).orElse(constructorDescription);
            if (description != null && description == constructorDescription) {
                description.markLazied(AnnotationUtil.hasAnnotation(parameter, Lazy.class));
            }
            Object resolveBean = autowiredProcessor.doResolve(SimpleGeneric.from(this.beanType, parameter), description, parameter.getType());
            constructorArgs.add(new Pair<>(parameter.getType(), resolveBean));
        }
        return constructorArgs;
    }

    public static Object resolvePlaceholderValue(String value, boolean bind, Type targetType) {
        ApplicationContext context = autowiredProcessor.getContext();
        PlaceholdersResolver placeholdersResolver = context.getBean(PlaceholdersResolver.class);
        GenericPropertiesContext propertiesContext = context.getBean(GenericPropertiesContext.class);
        return resolvePlaceholderValue(value, bind, targetType, placeholdersResolver, propertiesContext);
    }

    public static Object resolvePlaceholderValue(String value, boolean bind, Type targetType, PlaceholdersResolver placeholdersResolver, GenericPropertiesContext propertyContext) {
        String resolved = placeholdersResolver.resolvePlaceholders(value);
        SimpleGeneric targetGeneric = (SimpleGeneric) new SimpleGeneric(targetType).resolve();
        if (bind) {
            return propertyContext.getProperty(resolved, targetGeneric);
        }
        if (targetGeneric.isMapGeneric()) {
            throw new UnsupportedOperationException("@Value doesn't support Map type, please set @Value#bind() to true.");
        }
        if (targetGeneric.isSimpleGeneric() || targetGeneric.getResolveType() instanceof GenericArrayType) {
            return CommonUtil.split(resolved, propertyContext.getDataBinder().getBindPropertyDelimiter(), e -> convert(e, targetGeneric.getSimpleType()));
        }
        return convert(resolved, targetGeneric.getSimpleType());
    }
}
