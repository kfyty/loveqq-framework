package com.kfyty.boot.resolver;

import com.kfyty.boot.configuration.ApplicationContext;
import com.kfyty.support.autoconfig.BeanDefine;
import com.kfyty.support.autoconfig.BeanRefreshComplete;
import com.kfyty.support.autoconfig.ImportBeanDefine;
import com.kfyty.support.autoconfig.InitializingBean;
import com.kfyty.support.autoconfig.InstantiateBean;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Modifier;
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
public class AnnotationConfigResolver {

    private ApplicationContext applicationContext;

    private MethodAnnotationResolver methodAnnotationResolver;

    private FieldAnnotationResolver fieldAnnotationResolver;

    public static AnnotationConfigResolver create() {
        ApplicationContext applicationContext = ApplicationContext.create();
        AnnotationConfigResolver annotationConfigResolver = new AnnotationConfigResolver();
        annotationConfigResolver.applicationContext = applicationContext;
        annotationConfigResolver.methodAnnotationResolver = new MethodAnnotationResolver(applicationContext);
        annotationConfigResolver.fieldAnnotationResolver = new FieldAnnotationResolver(applicationContext);
        return annotationConfigResolver;
    }

    public ApplicationContext doResolver(Class<?> clazz, Set<Class<?>> scanClasses, Set<BeanDefine> beanDefines) {
        this.processImportBeanDefine(scanClasses, beanDefines);

        this.instantiateBeanDefine(beanDefines);

        this.fieldAnnotationResolver.doResolver(true);
        this.methodAnnotationResolver.doResolver();
        this.fieldAnnotationResolver.doResolver(false);

        this.processInstantiateBean(beanDefines);

        this.processRefreshComplete(clazz);

        return applicationContext;
    }

    private void instantiateBeanDefine(Set<BeanDefine> beanDefines) {
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

    private void processImportBeanDefine(Set<Class<?>> scanClasses, Set<BeanDefine> beanDefines) {
        Set<BeanDefine> importBeanDefines = beanDefines.stream().filter(e -> ImportBeanDefine.class.isAssignableFrom(e.getBeanType())).collect(Collectors.toSet());
        for (BeanDefine importBeanDefine : importBeanDefines) {
            ImportBeanDefine bean = (ImportBeanDefine) this.applicationContext.registerBean(importBeanDefine);
            beanDefines.addAll(bean.doImport(scanClasses));
        }
    }

    private void processInstantiateBean(Set<BeanDefine> beanDefines) {
        for (InstantiateBean bean : applicationContext.getBeanOfType(InstantiateBean.class).values()) {
            for (BeanDefine beanDefine : beanDefines) {
                if (bean.canInstantiate(beanDefine.getBeanType())) {
                    Object instance = bean.doInstantiate(beanDefine.getBeanType());
                    applicationContext.registerBean(bean.getBeanName(beanDefine.getBeanType()), beanDefine.getBeanType(), instance);
                    if(log.isDebugEnabled()) {
                        log.debug(": instantiate bean: [{}] !", instance);
                    }
                }
            }
        }

        for (InitializingBean bean : applicationContext.getBeanOfType(InitializingBean.class).values()) {
            bean.afterPropertiesSet();
        }
    }

    private void processRefreshComplete(Class<?> clazz) {
        for (BeanRefreshComplete bean : applicationContext.getBeanOfType(BeanRefreshComplete.class).values()) {
            bean.onComplete(clazz);
        }
    }
}
