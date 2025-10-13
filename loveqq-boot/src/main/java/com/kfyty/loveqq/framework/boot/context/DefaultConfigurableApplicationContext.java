package com.kfyty.loveqq.framework.boot.context;

import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPreProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ConfigurableApplicationContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.beans.filter.ComponentFilterDescription;
import com.kfyty.loveqq.framework.core.autoconfig.beans.filter.ComponentMatcher;
import com.kfyty.loveqq.framework.core.autoconfig.beans.filter.DefaultComponentMatcher;
import com.kfyty.loveqq.framework.core.autoconfig.boostrap.Bootstrap;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.io.FactoriesLoader;
import com.kfyty.loveqq.framework.core.support.AntPathMatcher;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * 描述: 可配置的应用上下文
 *
 * @author kfyty725
 * @date 2022/10/29 15:00
 * @email kfyty725@hotmail.com
 */
public class DefaultConfigurableApplicationContext extends AbstractApplicationContext implements ConfigurableApplicationContext {
    /**
     * 命令行
     */
    protected String[] commanderArgs;

    /**
     * 启动类资源
     */
    protected Class<?> primarySource;

    /**
     * 路径匹配器
     */
    protected PatternMatcher patternMatcher;

    /**
     * 自定义配置资源
     */
    protected List<Object> sources;

    /**
     * 已扫描的 class
     */
    protected Set<Class<?>> scannedClasses;

    /**
     * 包含组件过滤器
     */
    protected List<ComponentFilterDescription> includeFilters;

    /**
     * 排除组件过滤器
     */
    protected List<ComponentFilterDescription> excludeFilters;

    /**
     * 组件匹配器
     */
    protected List<ComponentMatcher> componentMatchers;

    public DefaultConfigurableApplicationContext() {
        super();
        this.patternMatcher = new AntPathMatcher();
        this.sources = new LinkedList<>();
        this.scannedClasses = new HashSet<>();
        this.includeFilters = new LinkedList<>();
        this.excludeFilters = new LinkedList<>();
        this.componentMatchers = new ArrayList<>(4);
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
    public List<Object> getSources() {
        return this.sources;
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
    public List<ComponentMatcher> getComponentMatcher() {
        return this.componentMatchers;
    }

    @Override
    public void setCommandLineArgs(String[] args) {
        this.commanderArgs = args;
    }

    @Override
    public void setPrimarySource(Class<?> primarySource) {
        this.primarySource = Objects.requireNonNull(primarySource, "Primary source can't null");
    }

    @Override
    public void setPatternMatcher(PatternMatcher patternMatcher) {
        this.patternMatcher = Objects.requireNonNull(patternMatcher);
    }

    @Override
    public void addSource(Object clazz) {
        this.sources.add(clazz);
    }

    @Override
    public void addSources(Collection<Object> classes) {
        this.sources.addAll(classes);
    }

    @Override
    public void addScannedClass(Class<?> clazz) {
        this.scannedClasses.add(clazz);
    }

    @Override
    public void addComponentFilter(ComponentFilterDescription componentFilter) {
        if (componentFilter.isInclude()) {
            this.includeFilters.add(componentFilter);
        } else {
            this.excludeFilters.add(componentFilter);
        }
    }

    @Override
    public void addComponentMatcher(ComponentMatcher componentMatcher) {
        this.invokeAwareMethod(componentMatcher.getClass().getName(), componentMatcher);
        this.componentMatchers.add(componentMatcher);
        this.componentMatchers.sort(Comparator.comparing(BeanUtil::getBeanOrder));
    }

    @Override
    public boolean isMatchComponent(Class<?> beanClass) {
        if (this.componentMatchers.isEmpty()) {
            this.addComponentMatcher(new DefaultComponentMatcher());
        }
        for (ComponentMatcher componentMatcher : this.componentMatchers) {
            if (componentMatcher.isMatch(beanClass, this.includeFilters, this.excludeFilters)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() {
        super.close();
        this.scannedClasses.clear();
        this.includeFilters.clear();
        this.excludeFilters.clear();
        this.componentMatchers.clear();
    }

    @Override
    protected void invokeAwareMethod(String beanName, Object bean) {
        super.invokeAwareMethod(beanName, bean);
        if (bean instanceof ConfigurableApplicationContextAware) {
            ((ConfigurableApplicationContextAware) bean).setConfigurableApplicationContext(this);
        }
    }

    @Override
    protected void prepareRefresh() {
        super.prepareRefresh();
        this.invokeBootstrap();
        this.loadBeanFactoryPreProcessorBeanDefinition();
    }

    /**
     * 执行启动引导
     */
    protected void invokeBootstrap() {
        List<Class<?>> bootstrapClasses = FactoriesLoader.loadFactories(Bootstrap.class).stream().map(ReflectUtil::load).sorted(Comparator.comparing(BeanUtil::getBeanOrder)).collect(Collectors.toList());
        for (Class<?> bootstrapClass : bootstrapClasses) {
            try {
                Bootstrap bootstrap = (Bootstrap) ReflectUtil.newInstance(bootstrapClass);
                bootstrap.bootstrap(this);
            } catch (Exception e) {
                throw new ResolvableException("Bootstrap failed, because " + e.getMessage(), e);
            }
        }
    }

    /**
     * 加载 {@link BeanFactoryPreProcessor}，前置处理 {@link BeanFactory}
     */
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
}
