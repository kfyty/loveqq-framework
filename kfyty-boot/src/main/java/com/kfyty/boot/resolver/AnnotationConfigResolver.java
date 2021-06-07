package com.kfyty.boot.resolver;

import com.kfyty.boot.beans.BeanResources;
import com.kfyty.boot.configuration.ApplicationContext;
import com.kfyty.mvc.annotation.Controller;
import com.kfyty.mvc.annotation.RestController;
import com.kfyty.support.autoconfig.BeanDefine;
import com.kfyty.support.autoconfig.BeanPostProcessor;
import com.kfyty.support.autoconfig.BeanRefreshComplete;
import com.kfyty.support.autoconfig.DestroyBean;
import com.kfyty.support.autoconfig.ImportBeanDefine;
import com.kfyty.support.autoconfig.InitializingBean;
import com.kfyty.support.autoconfig.InstantiateBean;
import com.kfyty.support.autoconfig.annotation.BootApplication;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.Repository;
import com.kfyty.support.autoconfig.annotation.Service;
import com.kfyty.support.utils.ReflectUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Modifier;
import java.util.HashSet;
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
        annotationConfigResolver.beanDefines = new HashSet<>();
        annotationConfigResolver.applicationContext = applicationContext;
        annotationConfigResolver.fieldAnnotationResolver = new FieldAnnotationResolver(annotationConfigResolver);
        annotationConfigResolver.methodAnnotationResolver = new MethodAnnotationResolver(annotationConfigResolver);
        return annotationConfigResolver;
    }

    public void addBeanDefine(BeanDefine beanDefine) {
        this.beanDefines.add(beanDefine);
    }

    public ApplicationContext doResolver(Class<?> clazz, Set<Class<?>> scanClasses, String ... args) {
        this.scanClasses = scanClasses;

        try {
            this.prepareBeanDefines();
            this.processImportBeanDefine();

            this.instantiateBeanDefine();

            this.fieldAnnotationResolver.doResolver(true);
            this.methodAnnotationResolver.doResolver();
            this.processCustomizeInstantiate();
            this.fieldAnnotationResolver.doResolver(false);

            this.processInstantiateBean();

            this.processRefreshComplete(clazz, args);

            Runtime.getRuntime().addShutdownHook(new Thread(this::processDestroy));

            return applicationContext;
        } catch (Throwable throwable) {
            log.error("k-boot started failed !");
            this.processDestroy();
            throw throwable;
        }
    }

    private void prepareBeanDefines() {
        this.scanClasses.stream()
                .filter(e -> !ReflectUtil.isAbstract(e))
                .filter(e ->
                        e.isAnnotationPresent(BootApplication.class) ||
                                e.isAnnotationPresent(Configuration.class) ||
                                e.isAnnotationPresent(Component.class) ||
                                e.isAnnotationPresent(Controller.class) ||
                                e.isAnnotationPresent(RestController.class) ||
                                e.isAnnotationPresent(Service.class) ||
                                e.isAnnotationPresent(Repository.class))
                .map(BeanDefine::new)
                .forEach(this::addBeanDefine);
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
            this.fieldAnnotationResolver.doResolver(bean.getClass(), bean, true);
            for (BeanDefine beanDefine : beanDefines) {
                if (bean.canInstantiate(beanDefine)) {
                    Object instance = bean.doInstantiate(beanDefine);
                    applicationContext.registerBean(bean.getBeanName(beanDefine), beanDefine.getBeanType(), instance);
                    if(log.isDebugEnabled()) {
                        log.debug(": customize instantiate bean: [{}] !", ReflectUtil.isJdkProxy(instance) ? beanDefine.getBeanType() : instance);
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
        for (BeanPostProcessor bean : applicationContext.getBeanOfType(BeanPostProcessor.class).values()) {
            for (BeanResources beanResources : applicationContext.getBeanResources().values()) {
                for (Map.Entry<String, Object> entry : beanResources.getBeans().entrySet()) {
                    Object newBean = bean.postProcessBeforeInitialization(entry.getValue(), entry.getKey());
                    if(newBean != null && newBean != entry.getValue()) {
                        applicationContext.replaceBean(entry.getKey(), beanResources.getBeanType(), newBean);
                    }
                }
            }
        }

        applicationContext.getBeanOfType(InitializingBean.class).values().forEach(InitializingBean::afterPropertiesSet);

        for (BeanDefine beanDefine : this.beanDefines) {
            if(beanDefine.getInitMethod() != null) {
                for (Object value : applicationContext.getBeanOfType(beanDefine.getBeanType()).values()) {
                    ReflectUtil.invokeMethod(value, beanDefine.getInitMethod());
                }
            }
        }

        for (BeanPostProcessor bean : applicationContext.getBeanOfType(BeanPostProcessor.class).values()) {
            for (BeanResources beanResources : applicationContext.getBeanResources().values()) {
                for (Map.Entry<String, Object> entry : beanResources.getBeans().entrySet()) {
                    Object newBean = bean.postProcessAfterInitialization(entry.getValue(), entry.getKey());
                    if(newBean != null && newBean != entry.getValue()) {
                        applicationContext.replaceBean(entry.getKey(), beanResources.getBeanType(), newBean);
                    }
                }
            }
        }
    }

    private void processRefreshComplete(Class<?> clazz, String ... args) {
        for (BeanRefreshComplete bean : applicationContext.getBeanOfType(BeanRefreshComplete.class).values()) {
            bean.onComplete(clazz, args);
        }
    }

    private void processDestroy() {
        log.info("destroy bean...");

        for (BeanPostProcessor bean : applicationContext.getBeanOfType(BeanPostProcessor.class).values()) {
            for (BeanResources beanResources : applicationContext.getBeanResources().values()) {
                for (Map.Entry<String, Object> entry : beanResources.getBeans().entrySet()) {
                    bean.postProcessBeforeDestroy(entry.getValue(), entry.getKey());
                }
            }
        }

        applicationContext.getBeanOfType(DestroyBean.class).values().forEach(DestroyBean::onDestroy);

        for (BeanDefine beanDefine : this.beanDefines) {
            if(beanDefine.getDestroyMethod() != null) {
                for (Object value : applicationContext.getBeanOfType(beanDefine.getBeanType()).values()) {
                    ReflectUtil.invokeMethod(value, beanDefine.getDestroyMethod());
                }
            }
        }
    }
}
