package com.kfyty.boot;

import com.kfyty.boot.context.factory.ApplicationContextFactory;
import com.kfyty.support.annotation.AnnotationWrapper;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.CommandLineRunner;
import com.kfyty.support.autoconfig.annotation.BootApplication;
import com.kfyty.support.autoconfig.annotation.ComponentFilter;
import com.kfyty.support.autoconfig.annotation.ComponentScan;
import com.kfyty.support.autoconfig.annotation.EnableAutoConfiguration;
import com.kfyty.support.autoconfig.annotation.Import;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.PackageUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

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
    private static final String META_FACTORIES = "META-INF/k.factories";
    private static final String META_FACTORIES_CONFIG = "com.kfyty.boot.auto.config";

    private final Class<?> primarySource;
    private final String[] commanderArgs;
    private final Set<Class<?>> scanClasses;
    private final Map<Class<?>, Set<Class<?>>> scanNestedClasses;
    private final Set<String> excludeBeanNames;
    private final Set<Class<?>> excludeBeanClasses;
    private final List<AnnotationWrapper<ComponentFilter>> includeFilterAnnotations;
    private final List<AnnotationWrapper<ComponentFilter>> excludeFilterAnnotations;

    private ApplicationContextFactory applicationContextFactory;

    public K(Class<?> clazz, String ... args) {
        Objects.requireNonNull(clazz);
        this.primarySource = clazz;
        this.commanderArgs = args;
        this.scanClasses = new HashSet<>();
        this.scanNestedClasses = new HashMap<>();
        this.excludeBeanNames = new HashSet<>(4);
        this.excludeBeanClasses = new HashSet<>(4);
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
        this.prepareMetaInfFactories();
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
            Set<Class<?>> classes = PackageUtil.scanClass(basePackage);
            for (Class<?> clazz : classes) {
                this.processScanBean(clazz);
            }
        }
    }

    @SneakyThrows
    private void prepareMetaInfFactories() {
        Enumeration<URL> urls = this.primarySource.getClassLoader().getResources(META_FACTORIES);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            Properties properties = new Properties();
            properties.load(url.openStream());
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                if (entry.getKey().toString().equals(META_FACTORIES_CONFIG)) {
                    Set<String> classes = CommonUtil.split(entry.getValue().toString(), ",", true);
                    for (String clazz : classes) {
                        this.processScanBean(Class.forName(clazz));
                    }
                }
            }
        }
    }

    private void processScanBean(Class<?> clazz) {
        if(this.scanClasses.contains(clazz)) {
            return;
        }
        this.scanClasses.add(clazz);
        ComponentScan componentScan = AnnotationUtil.findAnnotation(clazz, ComponentScan.class);
        if (componentScan != null) {
            this.prepareScanBean(new HashSet<>(Arrays.asList(componentScan.value())));
        }
        this.processAutoConfiguration(clazz);
    }

    private void processAutoConfiguration(Class<?> clazz) {
        if(AnnotationUtil.hasAnnotation(clazz, Import.class)) {
            Arrays.stream(AnnotationUtil.findAnnotation(clazz, Import.class).config()).forEach(this::processScanBean);
        }
        this.processComponentFilter(clazz, true, AnnotationUtil.findAnnotation(clazz, ComponentFilter.class));
        Optional.ofNullable(AnnotationUtil.findAnnotation(clazz, ComponentScan.class)).ifPresent(e -> {
            this.processComponentFilter(clazz, true, e.includeFilter());
            this.processComponentFilter(clazz, false, e.excludeFilter());
        });
        Optional.ofNullable(AnnotationUtil.findAnnotation(clazz, BootApplication.class)).ifPresent(e -> {
            excludeBeanNames.addAll(Arrays.asList(e.excludeNames()));
            excludeBeanClasses.addAll(Arrays.asList(e.exclude()));
        });
        Optional.ofNullable(AnnotationUtil.findAnnotation(clazz, EnableAutoConfiguration.class)).ifPresent(e -> {
            excludeBeanNames.addAll(Arrays.asList(e.excludeNames()));
            excludeBeanClasses.addAll(Arrays.asList(e.exclude()));
        });
        if(!AnnotationUtil.isMetaAnnotation(clazz)) {
            for (Annotation nestedAnnotation : AnnotationUtil.findAnnotations(clazz)) {
                this.processAutoConfiguration(nestedAnnotation.annotationType());
            }
        }
    }

    private void processComponentFilter(Object declaring, boolean isInclude, ComponentFilter componentFilter) {
        if(this.isEmptyComponentFilter(componentFilter)) {
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

    private boolean isEmptyComponentFilter(ComponentFilter filter) {
        return filter == null || CommonUtil.empty(filter.value()) && CommonUtil.empty(filter.classes()) && CommonUtil.empty(filter.annotations());
    }
}
