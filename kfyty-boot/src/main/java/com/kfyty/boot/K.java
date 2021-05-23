package com.kfyty.boot;

import com.kfyty.boot.configuration.ApplicationContext;
import com.kfyty.boot.resolver.AnnotationConfigResolver;
import com.kfyty.mvc.annotation.Controller;
import com.kfyty.mvc.annotation.RestController;
import com.kfyty.support.autoconfig.BeanDefine;
import com.kfyty.support.autoconfig.annotation.BootApplication;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.ComponentScan;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.EnableAutoConfiguration;
import com.kfyty.support.autoconfig.annotation.Import;
import com.kfyty.support.autoconfig.annotation.Repository;
import com.kfyty.support.autoconfig.annotation.Service;
import com.kfyty.util.CommonUtil;
import com.kfyty.util.PackageUtil;
import lombok.SneakyThrows;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 功能描述: 启动类
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/23 16:46
 * @since JDK 1.8
 */
public class K {
    private static final String META_FACTORIES = "META-INF/k.factories";
    private static final String META_FACTORIES_CONFIG = "com.kfyty.boot.auto.config";

    private final Set<Class<?>> scanBeans;
    private final Set<BeanDefine> beanDefines;
    private final Class<?> primarySource;

    public K(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        this.scanBeans = new HashSet<>();
        this.beanDefines = new HashSet<>();
        this.primarySource = clazz;
    }

    public ApplicationContext run() {
        HashSet<String> primaryPackage = new HashSet<>(Collections.singleton(primarySource.getPackage().getName()));
        this.prepareScanBean(primaryPackage);
        this.prepareMetaInfFactories();
        this.excludeScanBean();
        this.prepareBeanDefines();
        return AnnotationConfigResolver.create().doResolver(primarySource, scanBeans, beanDefines);
    }

    public static ApplicationContext run(Class<?> clazz, String ... args) {
        return new K(clazz).run();
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
        ComponentScan componentScan = clazz.getAnnotation(ComponentScan.class);
        if (componentScan != null) {
            this.prepareScanBean(new HashSet<>(Arrays.asList(componentScan.value())));
        }
        if(clazz.isAnnotationPresent(Import.class)) {
            for (Class<?> importClazz : clazz.getAnnotation(Import.class).config()) {
                this.processScanBean(importClazz);
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
                    Set<String> classes = Arrays.stream(entry.getValue().toString().split(",")).filter(e -> !CommonUtil.empty(e)).collect(Collectors.toSet());
                    for (String clazz : classes) {
                        this.processScanBean(Class.forName(clazz));
                    }
                }
            }
        }
    }

    private void excludeScanBean() {
        Set<Class<?>> excludeClasses = new HashSet<>();
        BootApplication bootApplication = this.primarySource.getAnnotation(BootApplication.class);
        if(bootApplication != null) {
            excludeClasses.addAll(Arrays.asList(bootApplication.exclude()));
        }
        for (Class<?> scanBean : this.scanBeans) {
            EnableAutoConfiguration autoConfiguration = scanBean.getAnnotation(EnableAutoConfiguration.class);
            if(autoConfiguration != null) {
                excludeClasses.addAll(Arrays.asList(autoConfiguration.exclude()));
            }
        }
        this.scanBeans.removeAll(excludeClasses);
    }

    private void prepareBeanDefines() {
        Set<BeanDefine> defines = this.scanBeans.stream()
                .filter(e -> !e.isInterface() && !Modifier.isAbstract(e.getModifiers()))
                .filter(e ->
                        e.isAnnotationPresent(BootApplication.class) ||
                                e.isAnnotationPresent(Configuration.class) ||
                                e.isAnnotationPresent(Component.class) ||
                                e.isAnnotationPresent(Controller.class) ||
                                e.isAnnotationPresent(RestController.class) ||
                                e.isAnnotationPresent(Service.class) ||
                                e.isAnnotationPresent(Repository.class))
                .map(BeanDefine::new)
                .collect(Collectors.toSet());
        this.beanDefines.addAll(defines);
    }
}
