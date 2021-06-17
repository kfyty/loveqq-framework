package com.kfyty.boot;

import com.kfyty.boot.resolver.AnnotationConfigResolver;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.BootApplication;
import com.kfyty.support.autoconfig.annotation.ComponentScan;
import com.kfyty.support.autoconfig.annotation.EnableAutoConfiguration;
import com.kfyty.support.autoconfig.annotation.Import;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.PackageUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
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
public class K {
    private static final String META_FACTORIES = "META-INF/k.factories";
    private static final String META_FACTORIES_CONFIG = "com.kfyty.boot.auto.config";
    private static final Set<String> excludeBeanNames = new HashSet<>(4);
    private static final Set<Class<?>> excludeBeanClasses = new HashSet<>(4);

    private final Set<Class<?>> scanBeans;
    private final Class<?> primarySource;

    public K(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        this.scanBeans = new HashSet<>();
        this.primarySource = clazz;
    }

    public ApplicationContext run(String ... args) {
        long start = System.currentTimeMillis();
        HashSet<String> primaryPackage = new HashSet<>(Collections.singleton(primarySource.getPackage().getName()));
        this.prepareScanBean(primaryPackage);
        this.prepareMetaInfFactories();
        this.excludeScanBean();
        ApplicationContext applicationContext = AnnotationConfigResolver.create(this.primarySource).doResolver(scanBeans, args);
        log.info("Started {} in {} seconds", this.primarySource.getSimpleName(), (System.currentTimeMillis() - start) / 1000D);
        return applicationContext;
    }

    public static boolean isExclude(String beanName) {
        return excludeBeanNames.contains(beanName);
    }

    public static boolean isExclude(Class<?> beanClass) {
        return excludeBeanClasses.contains(beanClass);
    }

    public static ApplicationContext run(Class<?> clazz, String ... args) {
        return new K(clazz).run(args);
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

    private void processScanBean(Class<?> clazz) {
        if(this.scanBeans.contains(clazz)) {
            return;
        }
        this.scanBeans.add(clazz);
        ComponentScan componentScan = AnnotationUtil.findAnnotation(clazz, ComponentScan.class);
        if (componentScan != null) {
            this.prepareScanBean(new HashSet<>(Arrays.asList(componentScan.value())));
        }
        if(AnnotationUtil.hasAnnotation(clazz, Import.class)) {
            for (Class<?> importClazz : AnnotationUtil.findAnnotation(clazz, Import.class).config()) {
                this.processScanBean(importClazz);
            }
        }
        for (Annotation annotation : AnnotationUtil.findAnnotations(clazz)) {
            if(AnnotationUtil.hasAnnotation(annotation.annotationType(), Import.class)) {
                for (Class<?> importClazz : AnnotationUtil.findAnnotation(annotation.annotationType(), Import.class).config()) {
                    this.processScanBean(importClazz);
                }
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

    private void excludeScanBean() {
        BootApplication bootApplication = AnnotationUtil.findAnnotation(this.primarySource, BootApplication.class);
        if(bootApplication != null) {
            excludeBeanNames.addAll(Arrays.asList(bootApplication.excludeNames()));
            excludeBeanClasses.addAll(Arrays.asList(bootApplication.exclude()));
        }
        for (Class<?> scanBean : this.scanBeans) {
            EnableAutoConfiguration autoConfiguration = AnnotationUtil.findAnnotation(scanBean, EnableAutoConfiguration.class);
            if(autoConfiguration != null) {
                excludeBeanNames.addAll(Arrays.asList(autoConfiguration.excludeNames()));
                excludeBeanClasses.addAll(Arrays.asList(autoConfiguration.exclude()));
            }
        }
        this.scanBeans.removeAll(excludeBeanClasses);
    }
}
