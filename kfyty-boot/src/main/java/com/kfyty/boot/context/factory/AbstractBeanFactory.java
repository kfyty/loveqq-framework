package com.kfyty.boot.context.factory;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.ApplicationContextAware;
import com.kfyty.core.autoconfig.BeanFactoryAware;
import com.kfyty.core.autoconfig.BeanPostProcessor;
import com.kfyty.core.autoconfig.DestroyBean;
import com.kfyty.core.autoconfig.InitializingBean;
import com.kfyty.core.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.autoconfig.beans.InstantiatedBeanDefinition;
import com.kfyty.core.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.core.exception.BeansException;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.BeanUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.core.wrapper.WeakKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kfyty.core.autoconfig.beans.BeanDefinition.BEAN_DEFINITION_COMPARATOR;
import static com.kfyty.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;
import static com.kfyty.core.utils.AnnotationUtil.hasAnnotationElement;
import static com.kfyty.core.utils.StreamUtil.throwMergeFunction;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

/**
 * 描述: bean 工厂基础实现
 *
 * @author kfyty725
 * @date 2021/7/3 10:36
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractBeanFactory implements ApplicationContextAware, BeanFactory {
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
    protected final List<BeanPostProcessor> beanPostProcessors;


    /**
     * 同一类型的 bean 定义缓存
     */
    protected final Map<WeakKey<Class<?>>, Map<String, BeanDefinition>> beanDefinitionsForType;

    /**
     * 应用上下文
     */
    protected ApplicationContext applicationContext;

    public AbstractBeanFactory() {
        this.beanDefinitions = Collections.synchronizedMap(new LinkedHashMap<>());
        this.beanInstances = new ConcurrentHashMap<>();
        this.beanReference = new ConcurrentHashMap<>();
        this.beanPostProcessors = new ArrayList<>();
        this.beanDefinitionsForType = Collections.synchronizedMap(new WeakHashMap<>());
    }

    public void registerBeanPostProcessors(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessors.add(beanPostProcessor);
    }

    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void registerBeanDefinition(BeanDefinition beanDefinition) {
        this.registerBeanDefinition(beanDefinition.getBeanName(), beanDefinition, true);
        for (Method method : ReflectUtil.getMethods(beanDefinition.getBeanType())) {
            Bean beanAnnotation = AnnotationUtil.findAnnotation(method, Bean.class);
            if (beanAnnotation != null) {
                this.registerBeanDefinition(genericBeanDefinition(beanDefinition, method, beanAnnotation).getBeanDefinition());
            }
        }
    }

    @Override
    public void registerBeanDefinition(String name, BeanDefinition beanDefinition) {
        if (this.containsBeanDefinition(beanDefinition.getBeanName())) {
            throw new BeansException("conflicting bean definition: " + beanDefinition.getBeanName());
        }
        this.beanDefinitions.putIfAbsent(name, beanDefinition);
    }

    @Override
    public void registerBeanDefinition(String name, BeanDefinition beanDefinition, boolean resolveCondition) {
        if (!resolveCondition) {
            this.registerBeanDefinition(name, beanDefinition);
            return;
        }
        this.registerConditionBeanDefinition(name, beanDefinition);
        this.beanDefinitionsForType.clear();
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
    public List<String> getBeanDefinitionNames(Class<?> beanType) {
        return this.getBeanDefinitions().values().stream().filter(e -> beanType.isAssignableFrom(e.getBeanType())).map(BeanDefinition::getBeanName).collect(Collectors.toList());
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return ofNullable(this.getBeanDefinitions().get(beanName)).orElseThrow(() -> new BeansException("no such bean definition found of name: " + beanName));
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
    public Map<String, BeanDefinition> getBeanDefinitions(Class<?> beanType) {
        return this.beanDefinitionsForType.computeIfAbsent(new WeakKey<>(beanType), k -> this.getBeanDefinitions(e -> beanType.isAssignableFrom(e.getValue().getBeanType())));
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitionWithAnnotation(Class<? extends Annotation> annotationClass) {
        return this.getBeanDefinitions(e -> hasAnnotationElement(e.getValue().getBeanType(), annotationClass));
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitions(Predicate<Map.Entry<String, BeanDefinition>> beanDefinitionPredicate) {
        return this.getBeanDefinitions().entrySet()
                .stream()
                .filter(beanDefinitionPredicate)
                .sorted((b1, b2) -> BEAN_DEFINITION_COMPARATOR.compare(b1.getValue(), b2.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, throwMergeFunction(), LinkedHashMap::new));
    }

    @Override
    public boolean contains(String name) {
        return this.beanInstances.containsKey(name);
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        Map<String, BeanDefinition> beanDefinitions = this.getBeanDefinitions(clazz);
        if (beanDefinitions.size() > 1) {
            throw new BeansException("more than one instance of type: " + clazz.getName());
        }
        return beanDefinitions.isEmpty() ? null : this.getBean(beanDefinitions.values().iterator().next().getBeanName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        if (this.contains(name)) {
            return (T) this.beanInstances.get(name);
        }
        return (T) this.registerBean(this.getBeanDefinition(name));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBeanOfType(Class<T> clazz) {
        Map<String, Object> beans = new LinkedHashMap<>(2);
        for (BeanDefinition beanDefinition : this.getBeanDefinitions(clazz).values()) {
            beans.put(beanDefinition.getBeanName(), this.registerBean(beanDefinition));
        }
        return (Map<String, T>) beans;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBeanWithAnnotation(Class<? extends Annotation> annotationClass) {
        Map<String, Object> beans = new LinkedHashMap<>(2);
        for (BeanDefinition beanDefinition : this.getBeanDefinitions().values()) {
            if (hasAnnotationElement(beanDefinition.getBeanType(), annotationClass)) {
                beans.put(beanDefinition.getBeanName(), this.registerBean(beanDefinition));
            }
        }
        return (Map<String, T>) beans;
    }

    /**
     * 根据 BeanDefinition 注册一个 bean
     * 由于创建 bean 实例的过程中可能会触发代理，因此需对返回的 bean 实例做二次判断
     *
     * @param beanDefinition BeanDefinition
     * @return bean 实例
     */
    @Override
    public Object registerBean(BeanDefinition beanDefinition) {
        if (this.contains(beanDefinition.getBeanName())) {
            return this.getBean(beanDefinition.getBeanName());
        }
        synchronized (this.beanInstances) {
            Object bean = ofNullable(this.beanReference.remove(beanDefinition.getBeanName())).orElseGet(() -> this.doCreateBean(beanDefinition));
            if (!this.contains(beanDefinition.getBeanName())) {
                return this.registerBean(beanDefinition.getBeanName(), bean);
            }
            return this.getBean(beanDefinition.getBeanName());
        }
    }

    @Override
    public Object registerBean(Class<?> clazz, Object bean) {
        return this.registerBean(BeanUtil.getBeanName(clazz), bean);
    }

    @Override
    public Object registerBean(String name, Object bean) {
        synchronized (this.beanInstances) {
            if (this.contains(name)) {
                throw new BeansException("conflicting bean name: " + name);
            }
            if (!this.containsBeanDefinition(name)) {
                this.registerBeanDefinition(InstantiatedBeanDefinition.from(name, bean.getClass()));
            }
            BeanDefinition beanDefinition = this.getBeanDefinition(name);
            if (beanDefinition.isSingleton()) {
                this.beanInstances.put(name, bean);
            }
            this.removeBeanReference(name);
            this.invokeAwareMethod(name, bean);
            bean = this.invokeBeanPostProcessAfterInstantiation(name, getExposedBean(beanDefinition, bean));
            this.autowiredBean(name, this.getExposedBean(beanDefinition, bean));
            bean = this.invokeLifecycleMethod(name, getExposedBean(beanDefinition, bean));
            return bean;
        }
    }

    @Override
    public void replaceBean(Class<?> clazz, Object bean) {
        this.replaceBean(BeanUtil.getBeanName(clazz), bean);
    }

    @Override
    public void replaceBean(String name, Object bean) {
        if (bean != null && this.contains(name)) {
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
            if (!this.containsReference(beanDefinition.getBeanName())) {
                this.beanReference.put(beanDefinition.getBeanName(), earlyBean);
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
            this.destroyBean(entry.getKey(), entry.getValue());
        }
        this.beanDefinitions.clear();
        this.beanInstances.clear();
        this.beanReference.clear();
        this.beanPostProcessors.clear();
        this.beanDefinitionsForType.clear();
        this.applicationContext = null;
    }

    @Override
    public void destroyBean(String name, Object bean) {
        this.getBeanPostProcessors().forEach(e -> e.postProcessBeforeDestroy(bean, name));

        if (bean instanceof DestroyBean) {
            ((DestroyBean) bean).onDestroy();
        }

        BeanDefinition beanDefinition = this.getBeanDefinition(name);

        if (beanDefinition instanceof MethodBeanDefinition) {
            MethodBeanDefinition methodBeanDefinition = (MethodBeanDefinition) beanDefinition;
            ofNullable(methodBeanDefinition.getDestroyMethod(bean)).ifPresent(e -> ReflectUtil.invokeMethod(bean, e));
        }

        this.beanReference.remove(name);
        this.beanInstances.remove(name);
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

    protected Object getExposedBean(BeanDefinition beanDefinition, Object bean) {
        return beanDefinition.isSingleton() ? this.getBean(beanDefinition.getBeanName()) : bean;
    }

    protected void invokeAwareMethod(String beanName, Object bean) {
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(this);
        }
    }

    protected Object invokeBeanPostProcessAfterInstantiation(String beanName, Object bean) {
        for (BeanPostProcessor beanPostProcessor : this.getBeanPostProcessors()) {
            if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                Object newBean = ((InstantiationAwareBeanPostProcessor) beanPostProcessor).postProcessAfterInstantiation(bean, beanName);
                if (newBean != null && newBean != bean) {
                    bean = newBean;
                    this.replaceBean(beanName, newBean);
                }
            }
        }
        return bean;
    }

    protected Object invokeLifecycleMethod(String beanName, Object bean) {
        if (bean instanceof ApplicationContext) {
            return bean;
        }
        bean = this.initializingBean(beanName, bean);
        return bean;
    }

    protected Object initializingBean(String beanName, Object bean) {
        BeanDefinition beanDefinition = this.getBeanDefinition(beanName);

        for (BeanPostProcessor beanPostProcessor : this.getBeanPostProcessors()) {
            Object newBean = beanPostProcessor.postProcessBeforeInitialization(bean, beanName);
            if (newBean != null && newBean != bean) {
                bean = newBean;
                this.replaceBean(beanName, newBean);
            }
        }

        if (bean instanceof InitializingBean) {
            ((InitializingBean) bean).afterPropertiesSet();
        }

        bean = this.getExposedBean(beanDefinition, bean);

        if (beanDefinition instanceof MethodBeanDefinition) {
            final Object finalBean = bean;
            MethodBeanDefinition methodBeanDefinition = (MethodBeanDefinition) beanDefinition;
            ofNullable(methodBeanDefinition.getInitMethod(bean)).ifPresent(e -> ReflectUtil.invokeMethod(finalBean, e));
        }

        bean = this.getExposedBean(beanDefinition, bean);

        for (BeanPostProcessor beanPostProcessor : this.getBeanPostProcessors()) {
            Object newBean = beanPostProcessor.postProcessAfterInitialization(bean, beanName);
            if (newBean != null && newBean != bean) {
                bean = newBean;
                this.replaceBean(beanName, newBean);
            }
        }

        return bean;
    }
}
