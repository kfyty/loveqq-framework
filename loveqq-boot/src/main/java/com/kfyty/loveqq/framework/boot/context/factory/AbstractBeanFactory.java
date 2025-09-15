package com.kfyty.loveqq.framework.boot.context.factory;

import com.kfyty.loveqq.framework.boot.autoconfig.factory.LazyProxyFactoryBean;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.BeanPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.DestroyBean;
import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.aware.BeanFactoryAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.InstantiatedBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.AutowiredProcessor;
import com.kfyty.loveqq.framework.core.exception.BeansException;
import com.kfyty.loveqq.framework.core.lang.util.concurrent.WeakConcurrentHashMap;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition.BEAN_DEFINITION_COMPARATOR;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;
import static com.kfyty.loveqq.framework.core.utils.StreamUtil.throwMergeFunction;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * 描述: bean 工厂基础实现
 *
 * @author kfyty725
 * @date 2021/7/3 10:36
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractBeanFactory implements ApplicationContextAware, BeanFactory {
    /**
     * 日志
     */
    protected static final Logger log = LoggerFactory.getLogger(BeanFactory.class);

    /**
     * map 工厂
     */
    protected static final Supplier<Map<String, BeanDefinition>> MAP_FACTORY = () -> new LinkedHashMap<>(2);

    /**
     * bean 定义
     */
    protected final Map<String, BeanDefinition> beanDefinitions;

    /**
     * bean 单例
     */
    protected final Map<String, Object> beanInstances;

    /**
     * 早期 bean 引用
     */
    protected final Map<String, Object> beanReference;

    /**
     * bean 后置处理器
     */
    protected final Map<String, BeanPostProcessor> beanPostProcessors;

    /**
     * 同一类型的 bean 定义缓存
     */
    protected final Map<Class<?>, BeanDefinition[]> beanDefinitionsForType;

    /**
     * 应用上下文
     */
    protected ApplicationContext applicationContext;

    /**
     * 获取当前创建中的 bean
     *
     * @return bean name
     */
    public static String getCreatingBean() {
        return AutowiredProcessor.CURRENT_CREATING_BEAN.get();
    }

    /**
     * 设置当前创建中的 bean
     *
     * @param beanName bean name
     */
    public static void setCreatingBean(String beanName) {
        AutowiredProcessor.CURRENT_CREATING_BEAN.set(beanName);
    }

    public AbstractBeanFactory() {
        this.beanDefinitions = new ConcurrentHashMap<>();
        this.beanInstances = new ConcurrentHashMap<>();
        this.beanReference = new ConcurrentHashMap<>();
        this.beanPostProcessors = Collections.synchronizedMap(new LinkedHashMap<>());
        this.beanDefinitionsForType = new WeakConcurrentHashMap<>();
    }

    public Collection<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors.values();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void registerBeanDefinition(BeanDefinition beanDefinition) {
        this.registerBeanDefinition(beanDefinition, true);
    }

    @Override
    public void registerBeanDefinition(BeanDefinition beanDefinition, boolean resolveNested) {
        // 注册 bean 定义
        this.registerBeanDefinition(beanDefinition.getBeanName(), beanDefinition, true);

        // 如果不解析嵌套的则直接返回
        if (!resolveNested) {
            return;
        }

        // 注册为了条件 bean 定义，解析嵌套的 bean 定义引用
        if (!this.containsBeanDefinition(beanDefinition.getBeanName())) {
            this.resolveNestedBeanDefinitionReference(beanDefinition);
            return;
        }

        // 解析嵌套的 bean 定义
        this.resolveRegisterNestedBeanDefinition(beanDefinition);
    }

    @Override
    public void registerBeanDefinition(String name, BeanDefinition beanDefinition) {
        BeanDefinition exists = this.beanDefinitions.putIfAbsent(name, beanDefinition);
        if (exists != null) {
            throw new BeansException(CommonUtil.format("Conflicting bean definition: [{}:{}] -> [{}:{}]", beanDefinition.getBeanName(), beanDefinition.getBeanType(), exists.getBeanName(), exists.getBeanType()));
        }
        this.beanDefinitionsForType.clear();
    }

    @Override
    public void registerBeanDefinition(String name, BeanDefinition beanDefinition, boolean resolveCondition) {
        if (!resolveCondition) {
            this.registerBeanDefinition(name, beanDefinition);
            return;
        }
        this.registerConditionBeanDefinition(name, beanDefinition);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanDefinitions.containsKey(beanName);
    }

    @Override
    public void removeBeanDefinition(String beanName) {
        this.beanDefinitions.remove(beanName);
    }

    @Override
    public Collection<String> getBeanDefinitionNames(Class<?> beanType) {
        return this.getBeanDefinitions(beanType).keySet();
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        BeanDefinition beanDefinition = this.beanDefinitions.get(beanName);
        if (beanDefinition != null) {
            return beanDefinition;
        }
        throw new BeansException("No such bean definition found of name: " + beanName);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName, Class<?> beanType) {
        Map<String, BeanDefinition> beanDefinitions = this.getBeanDefinitions(beanType);
        return beanDefinitions.size() == 1 ? beanDefinitions.values().iterator().next() : this.getBeanDefinition(beanName);
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitions() {
        return unmodifiableMap(this.beanDefinitions);
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitions(boolean isAutowireCandidate) {
        return this.stream(e -> e.isAutowireCandidate() == isAutowireCandidate).collect(toMap(BeanDefinition::getBeanName, identity(), throwMergeFunction(), LinkedHashMap::new));
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitions(Class<?> beanType) {
        BeanDefinition[] beanDefinitions = this.beanDefinitionsForType.computeIfAbsent(beanType, k -> this.stream(e -> beanType.isAssignableFrom(e.getBeanType())).toArray(BeanDefinition[]::new));
        return Arrays.stream(beanDefinitions).collect(toMap(BeanDefinition::getBeanName, identity(), throwMergeFunction(), MAP_FACTORY));
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitions(Class<?> beanType, boolean isAutowireCandidate) {
        return this.getBeanDefinitions(beanType)
                .values()
                .stream()
                .filter(e -> e.isAutowireCandidate() == isAutowireCandidate)
                .collect(toMap(BeanDefinition::getBeanName, identity(), throwMergeFunction(), MAP_FACTORY));
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitionWithAnnotation(Class<? extends Annotation> annotationClass) {
        return this.stream(e -> hasAnnotation(e.getBeanType(), annotationClass)).collect(toMap(BeanDefinition::getBeanName, identity(), throwMergeFunction(), LinkedHashMap::new));
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitionWithAnnotation(Class<? extends Annotation> annotationClass, boolean isAutowireCandidate) {
        return this.stream(e -> hasAnnotation(e.getBeanType(), annotationClass) && e.isAutowireCandidate() == isAutowireCandidate).collect(toMap(BeanDefinition::getBeanName, identity(), throwMergeFunction(), LinkedHashMap::new));
    }

    @Override
    public Stream<BeanDefinition> stream(Predicate<BeanDefinition> beanDefinitionPredicate) {
        return this.beanDefinitions.values().stream().filter(beanDefinitionPredicate).sorted(BEAN_DEFINITION_COMPARATOR);
    }

    @Override
    public boolean contains(String name) {
        return this.beanInstances.containsKey(name);
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return this.getBean(clazz, false);
    }

    @Override
    public <T> T getBean(Class<T> clazz, boolean isLazyInit) {
        Map<String, BeanDefinition> beanDefinitions = this.getBeanDefinitions(clazz, true);
        if (beanDefinitions.size() > 1) {
            throw new BeansException("There's more than one instance of type: " + clazz.getName());
        }
        return beanDefinitions.isEmpty() ? null : this.getBean(beanDefinitions.values().iterator().next(), isLazyInit);
    }

    @Override
    public <T> T getBean(String name) {
        return this.getBean(name, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, boolean isLazyInit) {
        T bean = (T) this.beanInstances.get(name);
        if (bean != null) {
            return bean;
        }
        return (T) this.registerBean(this.getBeanDefinition(name), isLazyInit);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(BeanDefinition beanDefinition, boolean isLazyInit) {
        T bean = (T) this.beanInstances.get(beanDefinition.getBeanName());
        if (bean != null) {
            return bean;
        }
        return (T) this.registerBean(beanDefinition, isLazyInit);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBeanOfType(Class<T> clazz) {
        Map<String, Object> beans = new LinkedHashMap<>(4);
        for (BeanDefinition beanDefinition : this.getBeanDefinitions(clazz).values()) {
            if (beanDefinition.isAutowireCandidate()) {
                beans.put(beanDefinition.getBeanName(), this.getBean(beanDefinition.getBeanName()));
            }
        }
        return (Map<String, T>) beans;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBeanWithAnnotation(Class<? extends Annotation> annotationClass) {
        Map<String, Object> beans = new LinkedHashMap<>(4);
        Map<String, BeanDefinition> beanDefinitions = this.getBeanDefinitionWithAnnotation(annotationClass);
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitions.entrySet()) {
            beans.put(entry.getKey(), this.registerBean(entry.getValue()));
        }
        return (Map<String, T>) beans;
    }

    @Override
    public void registerBeanPostProcessors(String beanName, BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessors.putIfAbsent(beanName, beanPostProcessor);
    }

    @Override
    public Object registerBean(BeanDefinition beanDefinition) {
        return this.registerBean(beanDefinition, false);
    }

    /**
     * 根据 BeanDefinition 注册一个 bean
     * 由于创建 bean 实例的过程中可能会触发代理，因此需对返回的 bean 实例做二次判断
     *
     * @param beanDefinition BeanDefinition
     * @param isLazyInit     是否延迟初始化，为 true 时返回一个延迟初始化代理
     * @return bean 实例
     */
    @Override
    public Object registerBean(BeanDefinition beanDefinition, boolean isLazyInit) {
        String beanName = beanDefinition.getBeanName();
        Object instance = this.beanInstances.get(beanName);
        if (instance != null) {
            return instance;
        }
        if (isLazyInit) {
            return new LazyProxyFactoryBean<>(beanDefinition).withBeanFactory(this).getObject();
        }
        synchronized (this.beanInstances) {
            if ((instance = this.beanInstances.get(beanName)) != null) {
                return instance;
            }
            final Object reference = this.beanReference.remove(beanName);
            final Object bean = reference != null ? reference : this.doCreateBean(beanDefinition);
            if ((instance = this.beanInstances.get(beanName)) != null) {
                return instance;
            }
            return this.registerBean(beanName, bean);
        }
    }

    @Override
    public Object registerBean(Class<?> clazz, Object bean) {
        return this.registerBean(BeanUtil.getBeanName(clazz), bean);
    }

    @Override
    public Object registerBean(String name, Object bean) {
        synchronized (this.beanInstances) {
            BeanDefinition beanDefinition = this.doRegisterBean(name, bean);
            bean = this.getExposedBean(beanDefinition, bean);
            this.autowiredBean(name, bean);
            return this.invokeLifecycle(beanDefinition, bean);
        }
    }

    @Override
    public void replaceBean(String name, Object bean) {
        if (bean != null && this.beanDefinitions.containsKey(name)) {
            this.beanInstances.put(name, bean);
        }
    }

    @Override
    public boolean containsReference(String name) {
        return this.beanReference.containsKey(name) || this.contains(name);
    }

    @Override
    public void registerBeanReference(BeanDefinition beanDefinition) {
        if (!this.containsReference(beanDefinition.getBeanName())) {
            Object earlyBean = this.doCreateBean(beanDefinition);
            if (!this.contains(beanDefinition.getBeanName())) {                                                         // 配置类可能已放入
                this.beanReference.putIfAbsent(beanDefinition.getBeanName(), earlyBean);
            }
        }
    }

    @Override
    public void removeBeanReference(String name) {
        this.beanReference.remove(name);
    }

    @Override
    public void close() {
        for (Map.Entry<String, Object> entry : this.beanInstances.entrySet()) {
            if (!(entry.getValue() instanceof BeanPostProcessor)) {
                this.destroyBean(entry.getKey(), entry.getValue());
            }
        }
        for (Iterator<Map.Entry<String, BeanPostProcessor>> i = this.beanPostProcessors.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, BeanPostProcessor> entry = i.next();
            this.destroyBean(entry.getKey(), entry.getValue());
            i.remove();
        }
        this.beanDefinitions.clear();
        this.beanInstances.clear();
        this.beanReference.clear();
        this.beanDefinitionsForType.clear();
        this.applicationContext = null;
    }

    @Override
    public void destroyBean(String name, Object bean) {
        this.destroyBean(this.getBeanDefinition(name), bean);
    }

    /**
     * 创建 bean 实例
     *
     * @param beanDefinition bean 定义
     * @return bean
     */
    public abstract Object doCreateBean(BeanDefinition beanDefinition);

    /**
     * 对 bean 执行依赖注入
     *
     * @param beanName bean 名称
     * @param bean     bean 实例
     */
    public abstract void autowiredBean(String beanName, Object bean);

    /**
     * 注册 bean 到 BeanFactory
     *
     * @param name bean name
     * @param bean bean
     * @return bean definition
     */
    protected BeanDefinition doRegisterBean(String name, Object bean) {
        synchronized (this.beanInstances) {
            BeanDefinition beanDefinition = this.beanDefinitions.get(name);
            if (beanDefinition == null) {
                beanDefinition = InstantiatedBeanDefinition.from(name, bean.getClass());
                this.registerBeanDefinition(name, beanDefinition, false);
            }

            if (!beanDefinition.getBeanType().isInstance(bean)) {
                throw new BeansException("The bean doesn't instance of " + beanDefinition.getBeanType());
            }

            if (beanDefinition.isSingleton()) {
                Object exists = this.beanInstances.putIfAbsent(name, bean);
                if (exists != null) {
                    throw new BeansException("Conflicting bean: " + name + " -> " + exists);
                }
            }
            this.removeBeanReference(name);
            this.invokeAwareMethod(name, bean);
            this.invokeBeanPostProcessAfterInstantiation(name, bean, beanDefinition);
            return beanDefinition;
        }
    }

    /**
     * 获取暴露的 bean，由于执行初始化方法，可能会修改 bean，因此需要根据 bean name 进行获取最新的
     *
     * @param beanDefinition bean definition
     * @param bean           当前 bean
     * @return 最新的 bean
     */
    protected Object getExposedBean(BeanDefinition beanDefinition, Object bean) {
        return beanDefinition.isSingleton() ? this.beanInstances.get(beanDefinition.getBeanName()) : bean;
    }

    protected void invokeAwareMethod(String beanName, Object bean) {
        if (bean instanceof BeanFactoryAware aware) {
            aware.setBeanFactory(this);
        }
    }

    protected void invokeBeanPostProcessAfterInstantiation(String beanName, Object bean, BeanDefinition beanDefinition) {
        for (BeanPostProcessor beanPostProcessor : this.getBeanPostProcessors()) {
            if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                Object newBean = ((InstantiationAwareBeanPostProcessor) beanPostProcessor).postProcessAfterInstantiation(bean, beanName, beanDefinition);
                if (newBean != null && newBean != bean) {
                    bean = newBean;
                    this.replaceBean(beanName, newBean);
                    this.invokeAwareMethod(beanName, bean);
                }
            }
        }
    }

    protected Object invokeLifecycle(BeanDefinition beanDefinition, Object bean) {
        if (beanDefinition instanceof FactoryBeanDefinition fbd) {
            FactoryBean<?> factoryBeanCache = FactoryBeanDefinition.getFactoryBeanCache(fbd.getFactoryBeanDefinition());
            if (!factoryBeanCache.shouldApplyLifecycle()) {
                return bean;
            }
        }
        return this.initializingBean(beanDefinition, bean);
    }

    protected Object initializingBean(BeanDefinition beanDefinition, Object bean) {
        String beanName = beanDefinition.getBeanName();

        for (BeanPostProcessor beanPostProcessor : this.getBeanPostProcessors()) {
            Object newBean = beanPostProcessor.postProcessBeforeInitialization(bean, beanName);
            if (newBean != null && newBean != bean) {
                bean = newBean;
                this.replaceBean(beanName, newBean);
            }
        }

        if (bean instanceof InitializingBean initializingBean) {
            initializingBean.afterPropertiesSet();
            bean = this.getExposedBean(beanDefinition, bean);
        }

        Method initMethod = beanDefinition.getInitMethod(bean);
        if (initMethod != null) {
            ReflectUtil.invokeMethod(bean, initMethod);
            bean = this.getExposedBean(beanDefinition, bean);
        }

        for (BeanPostProcessor beanPostProcessor : this.getBeanPostProcessors()) {
            Object newBean = beanPostProcessor.postProcessAfterInitialization(bean, beanName);
            if (newBean != null && newBean != bean) {
                bean = newBean;
                this.replaceBean(beanName, newBean);
            }
        }

        return bean;
    }

    protected void destroyBean(BeanDefinition beanDefinition, Object bean) {
        boolean applyLifeCycle = true;
        final String beanName = beanDefinition.getBeanName();

        if (beanDefinition instanceof FactoryBeanDefinition fbd) {
            FactoryBean<?> factoryBeanCache = FactoryBeanDefinition.getFactoryBeanCache(fbd.getFactoryBeanDefinition());
            applyLifeCycle = factoryBeanCache.shouldApplyLifecycle();
        }

        if (applyLifeCycle) {
            try {
                for (BeanPostProcessor beanPostProcessor : this.getBeanPostProcessors()) {
                    beanPostProcessor.postProcessBeforeDestroy(bean, beanName);
                }
            } catch (Throwable e) {
                log.error("Destroy bean error: {} -> {}", beanName, e.getMessage(), e);
            }

            if (bean instanceof DestroyBean destroyBean) {
                try {
                    destroyBean.destroy();
                } catch (Throwable e) {
                    log.error("Destroy bean error: {} -> {}", beanName, e.getMessage(), e);
                }
            }

            Method destroyMethod = beanDefinition.getDestroyMethod(bean);
            if (destroyMethod != null) {
                try {
                    ReflectUtil.invokeMethod(bean, destroyMethod);
                } catch (Throwable e) {
                    log.error("Destroy bean error: {} -> {}", beanName, e.getMessage(), e);
                }
            }
        }

        this.beanReference.remove(beanName);
        this.beanInstances.remove(beanName);
    }
}
