package com.kfyty.boot.context;

import com.kfyty.core.autoconfig.BeanFactoryPreProcessor;
import com.kfyty.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.core.autoconfig.aware.ConfigurableApplicationContextAware;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.filter.ComponentFilterDescription;
import com.kfyty.core.io.FactoriesLoader;
import com.kfyty.core.support.AntPathMatcher;
import com.kfyty.core.support.PatternMatcher;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.core.support.Pair;

import java.util.ArrayList;
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

    protected PatternMatcher patternMatcher;

    protected Set<Class<?>> scannedClasses;

    protected List<ComponentFilterDescription> includeFilters;

    protected List<ComponentFilterDescription> excludeFilters;

    public DefaultConfigurableApplicationContext() {
        super();
        this.patternMatcher = new AntPathMatcher();
        this.scannedClasses = new HashSet<>();
        this.includeFilters = new ArrayList<>(4);
        this.excludeFilters = new ArrayList<>(4);
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
    public PatternMatcher getPatternMatcher() {
        return this.patternMatcher;
    }

    @Override
    public Set<Class<?>> getScannedClasses() {
        return this.scannedClasses;
    }

    @Override
    public List<ComponentFilterDescription> getIncludeFilters() {
        return this.includeFilters;
    }

    @Override
    public List<ComponentFilterDescription> getExcludeFilters() {
        return this.excludeFilters;
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
    public void setPatternMatcher(PatternMatcher patternMatcher) {
        this.patternMatcher = Objects.requireNonNull(patternMatcher);
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
    public void addIncludeFilter(ComponentFilterDescription componentFilter) {
        this.includeFilters.add(componentFilter);
    }

    @Override
    public void addExcludeFilter(ComponentFilterDescription componentFilter) {
        this.excludeFilters.add(componentFilter);
    }

    @Override
    public boolean doFilterComponent(Class<?> beanClass) {
        Pair<Boolean, ComponentFilterDescription> exclude = this.doFilterComponent(this.excludeFilters, beanClass, false);
        if (!exclude.getKey() && exclude.getValue() != null) {
            return !doFilterComponent(exclude.getValue().getDeclare());
        }
        Pair<Boolean, ComponentFilterDescription> include = this.doFilterComponent(this.includeFilters, beanClass, true);
        return include.getKey();
    }

    @Override
    public void close() {
        super.close();
        this.scannedClasses.clear();
        this.includeFilters.clear();
        this.excludeFilters.clear();
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
            BeanDefinition beanDefinition = genericBeanDefinition(beanFactoryPreProcessorClass)
                    .setBeanName(beanFactoryPreProcessorClassName)
                    .getBeanDefinition();
            this.registerBeanDefinition(beanDefinition);
        }
    }

    /**
     * 根据组件过滤器进行匹配
     *
     * @param filters   组件过滤条件
     * @param beanClass 目标 bean class
     * @param isInclude 当前匹配排除还是包含
     * @return 匹配结果，以及对应的过滤组件
     */
    protected Pair<Boolean, ComponentFilterDescription> doFilterComponent(List<ComponentFilterDescription> filters, Class<?> beanClass, boolean isInclude) {
        for (ComponentFilterDescription filter : filters) {
            if (filter.getBasePackages().stream().anyMatch(e -> this.patternMatcher.matches(e, beanClass.getName()))) {
                return new Pair<>(isInclude, filter);
            }
            if (filter.getClasses().contains(beanClass)) {
                return new Pair<>(isInclude, filter);
            }
            if (filter.getAnnotations().stream().anyMatch(e -> hasAnnotationElement(beanClass, e))) {
                return new Pair<>(isInclude, filter);
            }
        }
        return new Pair<>(!isInclude, null);
    }
}
