package com.kfyty.boot.resolver;

import com.kfyty.boot.beans.BeanResources;
import com.kfyty.boot.configuration.ApplicationContext;
import com.kfyty.support.autoconfig.BeanDefine;
import com.kfyty.support.autoconfig.BeanPostProcessor;
import com.kfyty.support.autoconfig.BeanRefreshComplete;
import com.kfyty.support.autoconfig.DestroyBean;
import com.kfyty.support.autoconfig.ImportBeanDefine;
import com.kfyty.support.autoconfig.InitializingBean;
import com.kfyty.support.autoconfig.InstantiateBean;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 功能描述: 注解配置解析器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/23 16:56
 * @since JDK 1.8
 */
@Slf4j
@Getter
public class AnnotationConfigResolver {
    private Set<Class<?>> scanClasses;
    private Set<BeanDefine> beanDefines;
    private ApplicationContext applicationContext;
    private FieldAnnotationResolver fieldAnnotationResolver;
    private MethodAnnotationResolver methodAnnotationResolver;

    public static AnnotationConfigResolver create(Class<?> primarySource) {
        ApplicationContext applicationContext = ApplicationContext.create(primarySource);
        AnnotationConfigResolver annotationConfigResolver = new AnnotationConfigResolver();
        annotationConfigResolver.applicationContext = applicationContext;
        annotationConfigResolver.fieldAnnotationResolver = new FieldAnnotationResolver(annotationConfigResolver);
        annotationConfigResolver.methodAnnotationResolver = new MethodAnnotationResolver(annotationConfigResolver);
        return annotationConfigResolver;
    }

    public ApplicationContext doResolver(Class<?> clazz, Set<Class<?>> scanClasses, Set<BeanDefine> beanDefines) {
        this.scanClasses = scanClasses;
        this.beanDefines = beanDefines;

        try {
            this.processImportBeanDefine();

            this.instantiateBeanDefine();
            this.processCustomizeInstantiate();

            this.fieldAnnotationResolver.doResolver(true);
            this.methodAnnotationResolver.doResolver();
            this.fieldAnnotationResolver.doResolver(false);

            this.processInstantiateBean();

            this.processRefreshComplete(clazz);

            Runtime.getRuntime().addShutdownHook(new Thread(this::processDestroy));

            return applicationContext;
        } catch (Throwable throwable) {
            log.error("k-boot started failed !");
            this.processDestroy();
            throw throwable;
        }
    }

    private void instantiateBeanDefine() {
        for (BeanDefine beanDefine : beanDefines) {
            if(beanDefine.isInstance()) {
                continue;
            }
            Class<?> clazz = beanDefine.getBeanType();
            if(!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                this.applicationContext.registerBean(beanDefine);
            }
        }
    }

    private void processCustomizeInstantiate() {
        for (InstantiateBean bean : applicationContext.getBeanOfType(InstantiateBean.class).values()) {
            for (BeanDefine beanDefine : beanDefines) {
                if (bean.canInstantiate(beanDefine.getBeanType())) {
                    Object instance = bean.doInstantiate(beanDefine.getBeanType());
                    applicationContext.registerBean(bean.getBeanName(beanDefine.getBeanType()), beanDefine.getBeanType(), instance);
                    if(log.isDebugEnabled()) {
                        log.debug(": customize instantiate bean: [{}] !", instance);
                    }
                }
            }
        }
    }

    private void processImportBeanDefine() {
        Set<BeanDefine> importBeanDefines = beanDefines.stream().filter(e -> ImportBeanDefine.class.isAssignableFrom(e.getBeanType())).collect(Collectors.toSet());
        for (BeanDefine importBeanDefine : importBeanDefines) {
            ImportBeanDefine bean = (ImportBeanDefine) this.applicationContext.registerBean(importBeanDefine);
            beanDefines.addAll(bean.doImport(scanClasses));
        }
    }

    private void processInstantiateBean() {
        applicationContext.getBeanOfType(InitializingBean.class).values().forEach(InitializingBean::afterPropertiesSet);

        for (BeanPostProcessor bean : applicationContext.getBeanOfType(BeanPostProcessor.class).values()) {
            for (BeanResources beanResources : applicationContext.getBeanResources().values()) {
                for (Map.Entry<String, Object> entry : beanResources.getBeans().entrySet()) {
                    if(bean.canProcess(entry.getValue())) {
                        applicationContext.replaceBean(entry.getKey(), beanResources.getBeanType(), bean.postProcess(entry.getValue()));
                    }
                }
            }
        }
    }

    private void processRefreshComplete(Class<?> clazz) {
        for (BeanRefreshComplete bean : applicationContext.getBeanOfType(BeanRefreshComplete.class).values()) {
            bean.onComplete(clazz);
        }
    }

    private void processDestroy() {
        log.info("destroy bean...");
        applicationContext.getBeanOfType(DestroyBean.class).values().forEach(DestroyBean::onDestroy);
    }
}
