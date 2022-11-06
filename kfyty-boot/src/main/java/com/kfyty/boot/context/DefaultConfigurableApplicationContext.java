package com.kfyty.boot.context;

import com.kfyty.core.autoconfig.BeanFactoryPreProcessor;
import com.kfyty.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.core.autoconfig.aware.ConfigurableApplicationContextAware;
import com.kfyty.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.core.io.FactoriesLoader;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.core.wrapper.AnnotationWrapper;
import com.kfyty.core.wrapper.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.kfyty.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;
import static com.kfyty.core.utils.AnnotationUtil.hasAnnotationElement;

/**
 * 描述: 可配置的应用上下文
 *
 * @author kfyty725
 * @date 2022/10/29 15:00
 * @email kfyty725@hotmail.com
 */
public class DefaultConfigurableApplicationContext extends AbstractApplicationContext implements ConfigurableApplicationContext {
    protected String[] commanderArgs;

    protected Class<?> primarySource;

    protected Set<Class<?>> scannedClasses;

    protected List<AnnotationWrapper<ComponentFilter>> includeFilterAnnotations;

    protected List<AnnotationWrapper<ComponentFilter>> excludeFilterAnnotations;

    public DefaultConfigurableApplicationContext() {
        super();
        this.scannedClasses = new HashSet<>();
        this.includeFilterAnnotations = new ArrayList<>(4);
        this.excludeFilterAnnotations = new ArrayList<>(4);
    }

    @Override
    public String[] getCommandLineArgs() {
        return this.commanderArgs;
    }

    @Override
    public Class<?> getPrimarySource() {
        return this.primarySource;
    }

    @Override
    public Set<Class<?>> getScannedClasses() {
        return this.scannedClasses;
    }

    @Override
    public List<AnnotationWrapper<ComponentFilter>> getIncludeComponentFilters() {
        return this.includeFilterAnnotations;
    }

    @Override
    public List<AnnotationWrapper<ComponentFilter>> getExcludeComponentFilters() {
        return this.excludeFilterAnnotations;
    }

    @Override
    public void setCommandLineArgs(String[] args) {
        this.commanderArgs = args;
    }

    @Override
    public void setPrimarySource(Class<?> primarySource) {
        this.primarySource = Objects.requireNonNull(primarySource, "primary source can't null");
    }

    @Override
    public void addScannedClass(Class<?> clazz) {
        this.scannedClasses.add(clazz);
    }

    @Override
    public void addScannedClasses(Collection<Class<?>> classes) {
        this.scannedClasses.addAll(classes);
    }

    @Override
    public void addIncludeComponentFilter(AnnotationWrapper<ComponentFilter> componentFilter) {
        this.includeFilterAnnotations.add(componentFilter);
    }

    @Override
    public void addExcludeComponentFilter(AnnotationWrapper<ComponentFilter> componentFilter) {
        this.excludeFilterAnnotations.add(componentFilter);
    }

    @Override
    public boolean doFilterComponent(Class<?> beanClass) {
        Pair<Boolean, AnnotationWrapper<ComponentFilter>> exclude = this.doFilterComponent(this.excludeFilterAnnotations, beanClass, false);
        if (!exclude.getKey() && exclude.getValue() != null) {
            return !doFilterComponent(exclude.getValue().getDeclaring());
        }
        Pair<Boolean, AnnotationWrapper<ComponentFilter>> include = this.doFilterComponent(this.includeFilterAnnotations, beanClass, true);
        return include.getKey();
    }

    @Override
    protected void invokeAwareMethod(String beanName, Object bean) {
        super.invokeAwareMethod(beanName, bean);
        if (bean instanceof ConfigurableApplicationContextAware) {
            ((ConfigurableApplicationContextAware) bean).setConfigurableApplicationContext(this);
        }
    }

    @Override
    protected void beforeRefresh() {
        super.beforeRefresh();
        this.loadBeanFactoryPreProcessorBeanDefinition();
    }

    protected void loadBeanFactoryPreProcessorBeanDefinition() {
        for (String beanFactoryPreProcessorClassName : FactoriesLoader.loadFactories(BeanFactoryPreProcessor.class)) {
            Class<?> beanFactoryPreProcessorClass = ReflectUtil.load(beanFactoryPreProcessorClassName);
            this.addScannedClass(beanFactoryPreProcessorClass);
            this.registerBeanDefinition(genericBeanDefinition(beanFactoryPreProcessorClass).getBeanDefinition());
        }
    }

    /**
     * 根据组件过滤器进行匹配
     *
     * @param componentFilterWrappers 组件过滤条件
     * @param beanClass               目标 bean class
     * @param isInclude               当前匹配排除还是包含
     * @return 匹配结果，以及对应的过滤组件
     */
    protected Pair<Boolean, AnnotationWrapper<ComponentFilter>> doFilterComponent(List<AnnotationWrapper<ComponentFilter>> componentFilterWrappers, Class<?> beanClass, boolean isInclude) {
        for (AnnotationWrapper<ComponentFilter> componentFilterWrapper : componentFilterWrappers) {
            ComponentFilter componentFilter = componentFilterWrapper.get();
            if (Arrays.stream(componentFilter.value()).anyMatch(beanClass.getName()::startsWith)) {
                return new Pair<>(isInclude, componentFilterWrapper);
            }
            if (Arrays.asList(componentFilter.classes()).contains(beanClass)) {
                return new Pair<>(isInclude, componentFilterWrapper);
            }
            if (Arrays.stream(componentFilter.annotations()).anyMatch(e -> hasAnnotationElement(beanClass, e))) {
                return new Pair<>(isInclude, componentFilterWrapper);
            }
        }
        return new Pair<>(!isInclude, null);
    }
}
