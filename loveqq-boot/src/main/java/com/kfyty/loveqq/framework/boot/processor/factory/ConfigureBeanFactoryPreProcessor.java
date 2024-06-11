package com.kfyty.loveqq.framework.boot.processor.factory;

import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPreProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.BootApplication;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentScan;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.EnableAutoConfiguration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Import;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.beans.filter.ComponentFilterDescription;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;
import com.kfyty.loveqq.framework.core.io.FactoriesLoader;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.PackageUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.filter.ComponentFilterDescription.contains;

/**
 * 描述: 配置 Bean 工厂
 *
 * @author kfyty725
 * @date 2022/10/29 15:23
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Integer.MIN_VALUE)
public class ConfigureBeanFactoryPreProcessor implements BeanFactoryPreProcessor, InternalPriority {
    protected Set<String> excludeQualifierAutoConfigNames;

    protected ConfigurableApplicationContext applicationContext;

    public ConfigureBeanFactoryPreProcessor() {
        this.excludeQualifierAutoConfigNames = new HashSet<>(4);
    }

    @Override
    public boolean allowAutowired() {
        return false;
    }

    @Override
    public void preProcessBeanFactory(BeanFactory beanFactory) {
        if (beanFactory instanceof ConfigurableApplicationContext) {
            this.preProcessBeanFactory((ConfigurableApplicationContext) beanFactory);
        }
    }

    protected void preProcessBeanFactory(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.prepareScanBean(Collections.singleton(applicationContext.getPrimarySource().getPackage().getName()));
        this.prepareScanAutoConfigFactories();
    }

    protected void prepareScanAutoConfigFactories() {
        Set<String> factories = FactoriesLoader.loadFactories(EnableAutoConfiguration.class);
        for (String className : factories) {
            if (!this.excludeQualifierAutoConfigNames.contains(className)) {
                Optional.ofNullable(ReflectUtil.load(className, false, false)).ifPresent(e -> this.processScanBean(e, true));
            }
        }
    }

    protected void prepareScanBean(Set<String> basePackages) {
        for (String basePackage : basePackages) {
            PackageUtil.scanClass(basePackage).forEach(e -> this.processScanBean(e, false));
        }
    }

    protected void processScanBean(Class<?> clazz, boolean autoconfig) {
        Set<Class<?>> scannedClasses = this.applicationContext.getScannedClasses();
        if (scannedClasses.contains(clazz) || AnnotationUtil.isAnnotation(clazz)) {
            return;
        }
        if (!autoconfig && FactoriesLoader.loadFactories(EnableAutoConfiguration.class).contains(clazz.getName())) {
            return;
        }
        this.applicationContext.addScannedClass(clazz);
        if (AnnotationUtil.hasAnnotationElement(clazz, Component.class)) {
            ComponentScan componentScan = AnnotationUtil.findAnnotationElement(clazz, ComponentScan.class);
            if (componentScan != null) {
                this.prepareScanBean(new HashSet<>(Arrays.asList(componentScan.value())));
                if (this.applicationContext.getComponentMatcher().stream().noneMatch(e -> e.getClass() == componentScan.matcher())) {
                    this.applicationContext.addComponentMatcher(ReflectUtil.newInstance(componentScan.matcher()));
                }
            }
            this.processAutoConfiguration(clazz, null, autoconfig);
        }
    }

    protected void processAutoConfiguration(Class<?> clazz, AnnotationMetadata<?> wrapper, boolean autoconfig) {
        if (AnnotationUtil.hasAnnotation(clazz, Import.class)) {
            Arrays.stream(AnnotationUtil.findAnnotation(clazz, Import.class).config()).forEach(e -> this.processScanBean(e, autoconfig));
        }

        Object declaring = wrapper == null ? clazz : wrapper.getDeclaring();

        this.processComponentFilter(declaring, true, AnnotationUtil.findAnnotation(clazz, ComponentFilter.class));

        ComponentScan componentScan = AnnotationUtil.findAnnotation(clazz, ComponentScan.class);
        BootApplication bootApplication = AnnotationUtil.findAnnotation(clazz, BootApplication.class);
        EnableAutoConfiguration enableAutoConfiguration = AnnotationUtil.findAnnotation(clazz, EnableAutoConfiguration.class);

        if (componentScan != null) {
            this.processComponentFilter(declaring, true, componentScan.includeFilter());
            this.processComponentFilter(declaring, false, componentScan.excludeFilter());
        }

        if (bootApplication != null) {
            this.excludeQualifierAutoConfigNames.addAll(Arrays.asList(bootApplication.excludeNames()));
            this.excludeQualifierAutoConfigNames.addAll(Arrays.stream(bootApplication.exclude()).map(Class::getName).collect(Collectors.toList()));
        }

        if (enableAutoConfiguration != null) {
            this.excludeQualifierAutoConfigNames.addAll(Arrays.asList(enableAutoConfiguration.excludeNames()));
            this.excludeQualifierAutoConfigNames.addAll(Arrays.stream(enableAutoConfiguration.exclude()).map(Class::getName).collect(Collectors.toList()));
        }

        if (!AnnotationUtil.isMetaAnnotation(clazz)) {
            for (Annotation nestedAnnotation : AnnotationUtil.findAnnotations(clazz)) {
                this.processAutoConfiguration(nestedAnnotation.annotationType(), new AnnotationMetadata<>(declaring, nestedAnnotation), autoconfig);
            }
        }
    }

    protected void processComponentFilter(Object declaring, boolean isInclude, ComponentFilter componentFilter) {
        if (componentFilter == null || CommonUtil.empty(componentFilter.value()) && CommonUtil.empty(componentFilter.classes()) && CommonUtil.empty(componentFilter.annotations())) {
            return;
        }
        ComponentFilterDescription filter = ComponentFilterDescription.from((Class<?>) declaring, componentFilter);
        if (isInclude && !contains(this.applicationContext.getIncludeFilters(), componentFilter)) {
            this.applicationContext.addIncludeFilter(filter);
        }
        if (!isInclude && !contains(this.applicationContext.getExcludeFilters(), componentFilter)) {
            this.applicationContext.addExcludeFilter(filter);
        }
    }
}
