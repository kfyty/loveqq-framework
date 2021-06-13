package com.kfyty.boot.resolver;

import com.kfyty.boot.K;
import com.kfyty.boot.beans.BeanResources;
import com.kfyty.boot.configuration.ApplicationContext;
import com.kfyty.mvc.annotation.Controller;
import com.kfyty.mvc.annotation.RestController;
import com.kfyty.support.autoconfig.BeanPostProcessor;
import com.kfyty.support.autoconfig.BeanRefreshComplete;
import com.kfyty.support.autoconfig.DestroyBean;
import com.kfyty.support.autoconfig.ImportBeanDefine;
import com.kfyty.support.autoconfig.InitializingBean;
import com.kfyty.support.autoconfig.annotation.BootApplication;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.Repository;
import com.kfyty.support.autoconfig.annotation.Service;
import com.kfyty.support.autoconfig.beans.AutowiredProcessor;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.FactoryBean;
import com.kfyty.support.autoconfig.beans.GenericBeanDefinition;
import com.kfyty.support.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
    private Map<String, BeanDefinition> beanDefinitions;
    private ApplicationContext applicationContext;
    private AutowiredProcessor autowiredProcessor;
    private FieldAnnotationResolver fieldAnnotationResolver;
    private MethodAnnotationResolver methodAnnotationResolver;

    public static AnnotationConfigResolver create(Class<?> primarySource) {
        AnnotationConfigResolver configResolver = new AnnotationConfigResolver();
        configResolver.beanDefinitions = new LinkedHashMap<>();
        configResolver.applicationContext = new ApplicationContext(primarySource, configResolver);
        configResolver.autowiredProcessor = new AutowiredProcessor(configResolver.applicationContext);
        configResolver.fieldAnnotationResolver = new FieldAnnotationResolver(configResolver);
        configResolver.methodAnnotationResolver = new MethodAnnotationResolver(configResolver);
        return configResolver;
    }

    public void addBeanDefinition(BeanDefinition beanDefinition) {
        if(beanDefinitions.containsKey(beanDefinition.getBeanName())) {
            throw new IllegalArgumentException("conflicting bean definition: " + beanDefinition);
        }
        this.beanDefinitions.put(beanDefinition.getBeanName(), beanDefinition);
        if(FactoryBean.class.isAssignableFrom(beanDefinition.getBeanType())) {
            BeanDefinition factoryBeanDefinition = GenericBeanDefinition.from(beanDefinition);
            this.beanDefinitions.put(factoryBeanDefinition.getBeanName(), factoryBeanDefinition);
            this.methodAnnotationResolver.prepareBeanDefines(factoryBeanDefinition);
        }
        this.methodAnnotationResolver.prepareBeanDefines(beanDefinition);
    }

    public ApplicationContext doResolver(Class<?> clazz, Set<Class<?>> scanClasses, String ... args) {
        this.scanClasses = scanClasses;

        try {
            this.prepareBeanDefines();
            this.processImportBeanDefinition();
            this.excludeBeanDefinition();
            this.sortBeanDefinition();

            this.instantiateBeanDefinition();

            this.fieldAnnotationResolver.doResolver();
            this.methodAnnotationResolver.doResolver();

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
                .map(e -> {
                    for (Annotation annotation : e.getAnnotations()) {
                        if (annotation.annotationType().isAnnotationPresent(Component.class)) {
                            String beanName = (String) ReflectUtil.invokeSimpleMethod(annotation, "value");
                            if (CommonUtil.notEmpty(beanName)) {
                                return GenericBeanDefinition.from(beanName, e);
                            }
                        }
                    }
                    return GenericBeanDefinition.from(e);
                })
                .forEach(this::addBeanDefinition);
    }

    private void processImportBeanDefinition() {
        Set<BeanDefinition> importBeanDefines = beanDefinitions.values().stream().filter(e -> ImportBeanDefine.class.isAssignableFrom(e.getBeanType())).collect(Collectors.toSet());
        for (BeanDefinition importBeanDefine : importBeanDefines) {
            ImportBeanDefine bean = (ImportBeanDefine) this.applicationContext.registerBean(importBeanDefine);
            bean.doImport(scanClasses).forEach(this::addBeanDefinition);
        }
    }

    private void excludeBeanDefinition() {
        Iterator<BeanDefinition> iterator = this.beanDefinitions.values().iterator();
        while (iterator.hasNext()) {
            BeanDefinition beanDefinition = iterator.next();
            if(K.isExclude(beanDefinition.getBeanName()) || K.isExclude(beanDefinition.getBeanType())) {
                iterator.remove();
                log.info("exclude bean definition: {}", beanDefinition);
            }
        }
    }

    private void sortBeanDefinition() {
        this.beanDefinitions = this.beanDefinitions.values()
                .stream()
                .sorted(Comparator.comparing(BeanUtil::getBeanOrder))
                .collect(Collectors.toMap(BeanDefinition::getBeanName, Function.identity(), (k1, k2) -> {
                    throw new IllegalStateException("Duplicate key " + k2);
                }, LinkedHashMap::new));
    }

    private void instantiateBeanDefinition() {
        for (BeanDefinition beanDefinition : beanDefinitions.values()) {
            this.applicationContext.registerBean(beanDefinition);
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

        Iterator<BeanDefinition> iterator = this.beanDefinitions.values().stream().filter(e -> e instanceof MethodBeanDefinition).iterator();
        while (iterator.hasNext()) {
            MethodBeanDefinition beanDefinition = (MethodBeanDefinition) iterator.next();
            if(beanDefinition.getInitMethod() != null) {
                for (Object value : applicationContext.getBeanOfType(beanDefinition.getBeanType()).values()) {
                    ReflectUtil.invokeMethod(value, beanDefinition.getInitMethod());
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

        Iterator<BeanDefinition> iterator = this.beanDefinitions.values().stream().filter(e -> e instanceof MethodBeanDefinition).iterator();
        while (iterator.hasNext()) {
            MethodBeanDefinition beanDefinition = (MethodBeanDefinition) iterator.next();
            if(beanDefinition.getDestroyMethod() != null) {
                for (Object value : applicationContext.getBeanOfType(beanDefinition.getBeanType()).values()) {
                    ReflectUtil.invokeMethod(value, beanDefinition.getDestroyMethod());
                }
            }
        }
    }
}
