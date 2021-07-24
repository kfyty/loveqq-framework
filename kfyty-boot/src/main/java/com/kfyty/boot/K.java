package com.kfyty.boot;

import com.kfyty.boot.context.factory.ApplicationContextFactory;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.CommandLineRunner;
import com.kfyty.support.autoconfig.annotation.BootApplication;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.ComponentFilter;
import com.kfyty.support.autoconfig.annotation.ComponentScan;
import com.kfyty.support.autoconfig.annotation.EnableAutoConfiguration;
import com.kfyty.support.autoconfig.annotation.Import;
import com.kfyty.support.io.FactoriesLoader;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.PackageUtil;
import com.kfyty.support.utils.ReflectUtil;
import com.kfyty.support.wrapper.AnnotationWrapper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 功能描述: 启动类
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/23 16:46
 * @since JDK 1.8
 */
@Slf4j
@Getter
public class K {
    private final Class<?> primarySource;
    private final String[] commanderArgs;
    private final Set<Class<?>> scanClasses;
    private final Set<String> excludeQualifierAutoConfigNames;
    private final List<AnnotationWrapper<ComponentFilter>> includeFilterAnnotations;
    private final List<AnnotationWrapper<ComponentFilter>> excludeFilterAnnotations;

    private ApplicationContextFactory applicationContextFactory;

    public K(Class<?> clazz, String ... args) {
        Objects.requireNonNull(clazz);
        this.primarySource = clazz;
        this.commanderArgs = args;
        this.scanClasses = new HashSet<>();
        this.excludeQualifierAutoConfigNames = new HashSet<>(4);
        this.includeFilterAnnotations = new ArrayList<>(4);
        this.excludeFilterAnnotations = new ArrayList<>(4);
        this.applicationContextFactory = new ApplicationContextFactory();
    }

    public void setApplicationContextFactory(ApplicationContextFactory applicationContextFactory) {
        this.applicationContextFactory = applicationContextFactory;
    }

    public ApplicationContext run() {
        long start = System.currentTimeMillis();
        this.prepareScanBean(Collections.singleton(primarySource.getPackage().getName()));
        this.prepareScanAutoConfigFactories();
        ApplicationContext applicationContext = this.applicationContextFactory.create(this).refresh();
        log.info("Started {} in {} seconds", this.primarySource.getSimpleName(), (System.currentTimeMillis() - start) / 1000D);
        this.invokeRunner(applicationContext);
        return applicationContext;
    }

    public static ApplicationContext run(Class<?> clazz, String ... args) {
        return new K(clazz, args).run();
    }

    private void invokeRunner(ApplicationContext applicationContext) {
        applicationContext.getBeanOfType(CommandLineRunner.class).values().forEach(e -> {
            try {
                e.run(this.commanderArgs);
            } catch (Exception ex) {
                throw new IllegalStateException("failed to execute CommandLineRunner", ex);
            }
        });
    }

    @SneakyThrows
    private void prepareScanBean(Set<String> basePackages) {
        for (String basePackage : basePackages) {
            PackageUtil.scanClass(basePackage).forEach(this::processScanBean);
        }
    }

    private void prepareScanAutoConfigFactories() {
        Set<String> factories = FactoriesLoader.loadFactories(EnableAutoConfiguration.class);
        for (String className : factories) {
            if(!this.excludeQualifierAutoConfigNames.contains(className)) {
                Optional.ofNullable(ReflectUtil.load(className, false)).ifPresent(this::processScanBean);
            }
        }
    }

    private void processScanBean(Class<?> clazz) {
        if(this.scanClasses.contains(clazz) || AnnotationUtil.isAnnotation(clazz)) {
            return;
        }
        this.scanClasses.add(clazz);
        if (AnnotationUtil.hasAnnotationElement(clazz, Component.class)) {
            ComponentScan componentScan = AnnotationUtil.findAnnotation(clazz, ComponentScan.class);
            if (componentScan != null) {
                this.prepareScanBean(new HashSet<>(Arrays.asList(componentScan.value())));
            }
            this.processAutoConfiguration(clazz, null);
        }
    }

    private void processAutoConfiguration(Class<?> clazz, AnnotationWrapper<?> wrapper) {
        if(AnnotationUtil.hasAnnotation(clazz, Import.class)) {
            Arrays.stream(AnnotationUtil.findAnnotation(clazz, Import.class).config()).forEach(this::processScanBean);
        }
        Object declaring = wrapper == null ? clazz : wrapper.getDeclaring();
        this.processComponentFilter(declaring, true, AnnotationUtil.findAnnotation(clazz, ComponentFilter.class));
        Optional.ofNullable(AnnotationUtil.findAnnotation(clazz, ComponentScan.class)).ifPresent(e -> {
            this.processComponentFilter(declaring, true, e.includeFilter());
            this.processComponentFilter(declaring, false, e.excludeFilter());
        });
        Optional.ofNullable(AnnotationUtil.findAnnotation(clazz, BootApplication.class)).ifPresent(e -> {
            excludeQualifierAutoConfigNames.addAll(Arrays.asList(e.excludeNames()));
            excludeQualifierAutoConfigNames.addAll(Arrays.stream(e.exclude()).map(Class::getName).collect(Collectors.toList()));
        });
        Optional.ofNullable(AnnotationUtil.findAnnotation(clazz, EnableAutoConfiguration.class)).ifPresent(e -> {
            excludeQualifierAutoConfigNames.addAll(Arrays.asList(e.excludeNames()));
            excludeQualifierAutoConfigNames.addAll(Arrays.stream(e.exclude()).map(Class::getName).collect(Collectors.toList()));
        });
        if(!AnnotationUtil.isMetaAnnotation(clazz)) {
            for (Annotation nestedAnnotation : AnnotationUtil.findAnnotations(clazz)) {
                this.processAutoConfiguration(nestedAnnotation.annotationType(), new AnnotationWrapper<>(declaring, nestedAnnotation));
            }
        }
    }

    private void processComponentFilter(Object declaring, boolean isInclude, ComponentFilter componentFilter) {
        if(componentFilter == null || CommonUtil.empty(componentFilter.value()) && CommonUtil.empty(componentFilter.classes()) && CommonUtil.empty(componentFilter.annotations())) {
            return;
        }
        AnnotationWrapper<ComponentFilter> filter = new AnnotationWrapper<>(declaring, componentFilter);
        if(isInclude && !AnnotationWrapper.contains(this.includeFilterAnnotations, componentFilter)) {
            includeFilterAnnotations.add(filter);
        }
        if(!isInclude && !AnnotationWrapper.contains(this.excludeFilterAnnotations, componentFilter)) {
            excludeFilterAnnotations.add(filter);
        }
    }
}
