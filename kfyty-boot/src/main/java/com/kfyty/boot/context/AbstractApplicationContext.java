package com.kfyty.boot.context;

import com.kfyty.boot.context.factory.AbstractAutowiredBeanFactory;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.BeanPostProcessor;
import com.kfyty.support.autoconfig.ContextAfterRefreshed;
import com.kfyty.support.autoconfig.ContextRefreshCompleted;
import com.kfyty.support.autoconfig.ImportBeanDefine;
import com.kfyty.support.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.ComponentFilter;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.autoconfig.beans.AutowiredCapableSupport;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.BeanFactory;
import com.kfyty.support.autoconfig.beans.builder.BeanDefinitionBuilder;
import com.kfyty.support.event.ApplicationEvent;
import com.kfyty.support.event.ApplicationEventPublisher;
import com.kfyty.support.event.ApplicationListener;
import com.kfyty.support.exception.BeansException;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.ReflectUtil;
import com.kfyty.support.wrapper.AnnotationWrapper;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 描述: 上下文基础实现
 *
 * @author kfyty725
 * @date 2021/7/3 11:05
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class AbstractApplicationContext extends AbstractAutowiredBeanFactory implements ApplicationContext {
    private static final Comparator<BeanDefinition> BEAN_DEFINITION_COMPARATOR = Comparator
            .comparing((BeanDefinition e) -> InstantiationAwareBeanPostProcessor.class.isAssignableFrom(e.getBeanType()) ? Order.HIGHEST_PRECEDENCE : Order.LOWEST_PRECEDENCE)
            .thenComparing(e -> BeanUtil.getBeanOrder((BeanDefinition) e));

    protected String[] commanderArgs;
    protected Class<?> primarySource;
    protected Set<Class<?>> scanClasses;
    protected List<AnnotationWrapper<ComponentFilter>> includeFilterAnnotations = new ArrayList<>(4);
    protected List<AnnotationWrapper<ComponentFilter>> excludeFilterAnnotations = new ArrayList<>(4);

    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;

    protected void beforeRefresh() {
        this.registerDefaultBean();
    }

    protected void onRefresh() {

    }

    protected void afterRefresh() {
        if (this.autowiredCapableSupport == null) {
            throw new BeansException("no bean instance found of type: " + AutowiredCapableSupport.class);
        }
        this.autowiredCapableSupport.doAutowiredLazy();
    }

    protected void registerDefaultBean() {
        this.registerBean(ApplicationContext.class, this);
        this.registerBean(BeanFactory.class, this);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = this;
    }

    @Override
    public Class<?> getPrimarySource() {
        return this.primarySource;
    }

    @Override
    public ApplicationContext refresh() {
        synchronized (this) {
            try {
                /* 刷新前的准备，由子类扩展 */
                this.beforeRefresh();

                /* 解析 bean 定义 */
                this.prepareBeanDefines(this.scanClasses);

                /* 注册 bean 后置处理器 */
                this.registerBeanPostProcessors();

                /* 导入自定义的 bean 定义 */
                this.processImportBeanDefinition(this.scanClasses);

                /* 实例化单例 bean 定义 */
                this.instantiateBeanDefinition();

                /* 子类扩展 */
                this.onRefresh();

                /* 子类扩展 */
                this.afterRefresh();

                /* 回调 Context 刷新后的接口 */
                this.invokeContextRefreshed();

                /* 添加销毁钩子 */
                Runtime.getRuntime().addShutdownHook(new Thread(this::close));

                return this;
            } catch (Throwable throwable) {
                log.error("k-boot started failed !");
                try {
                    this.close();
                } catch (Throwable nestedThrowable) {
                    log.error("process destroy error !", nestedThrowable);
                }
                throw throwable;
            }
        }
    }

    /**
     * 根据组件过滤器进行匹配
     *      排除过滤：
     *          若返回 true，则排除过滤匹配失败，继续执行包含过滤
     *          若返回 false，说明可能被排除，此时需继续判断该注解的声明是否被排除
     *       包含过滤：
     *          直接返回即可
     * @param beanClass 目标 bean class
     * @return 该 bean class 是否能够生成 bean 定义
     */
    @Override
    public boolean doFilterComponent(Class<?> beanClass) {
        Pair<Boolean, AnnotationWrapper<ComponentFilter>> exclude = doFilterComponent(this.excludeFilterAnnotations, beanClass, false);
        if(!exclude.getKey() && exclude.getValue() != null) {
            return !doFilterComponent(exclude.getValue().getDeclaring());
        }
        Pair<Boolean, AnnotationWrapper<ComponentFilter>> include = doFilterComponent(this.includeFilterAnnotations, beanClass, true);
        return include.getKey();
    }

    @Override
    public void publishEvent(ApplicationEvent<?> event) {
        this.applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void registerEventListener(ApplicationListener<?> applicationListener) {
        this.applicationEventPublisher.registerEventListener(applicationListener);
    }

    /**
     * 根据组件过滤器进行匹配
     * @param componentFilterWrappers 组件过滤条件
     * @param beanClass 目标 bean class
     * @param isInclude 当前匹配排除还是包含
     * @return 匹配结果，以及对应的过滤组件
     */
    protected Pair<Boolean, AnnotationWrapper<ComponentFilter>> doFilterComponent(List<AnnotationWrapper<ComponentFilter>> componentFilterWrappers, Class<?> beanClass, boolean isInclude) {
        for (AnnotationWrapper<ComponentFilter> componentFilterWrapper : componentFilterWrappers) {
            ComponentFilter componentFilter = componentFilterWrapper.get();
            if(Arrays.stream(componentFilter.value()).anyMatch(beanClass.getName()::startsWith)) {
                return new Pair<>(isInclude, componentFilterWrapper);
            }
            if(Arrays.asList(componentFilter.classes()).contains(beanClass)) {
                return new Pair<>(isInclude, componentFilterWrapper);
            }
            if(Arrays.stream(componentFilter.annotations()).anyMatch(e -> AnnotationUtil.hasAnnotationElement(beanClass, e))) {
                return new Pair<>(isInclude, componentFilterWrapper);
            }
        }
        return new Pair<>(!isInclude, null);
    }

    protected void prepareBeanDefines(Set<Class<?>> scanClasses) {
        scanClasses.stream().filter(e -> !ReflectUtil.isAbstract(e) && this.doFilterComponent(e)).map(e -> BeanDefinitionBuilder.genericBeanDefinition(e).getBeanDefinition()).forEach(this::registerBeanDefinition);
    }

    protected void processImportBeanDefinition(Set<Class<?>> scanClasses) {
        Set<BeanDefinition> importBeanDefines = this.getBeanDefinitions().values().stream().filter(e -> ImportBeanDefine.class.isAssignableFrom(e.getBeanType())).sorted(Comparator.comparing(BeanUtil::getBeanOrder)).collect(Collectors.toCollection(LinkedHashSet::new));
        for (BeanDefinition importBeanDefine : importBeanDefines) {
            ImportBeanDefine bean = (ImportBeanDefine) this.registerBean(importBeanDefine);
            bean.doImport(scanClasses).forEach(this::registerBeanDefinition);
        }
    }

    protected void registerBeanPostProcessors() {
        this.getBeanDefinitions().values()
                .stream()
                .filter(e -> BeanPostProcessor.class.isAssignableFrom(e.getBeanType()))
                .sorted(BEAN_DEFINITION_COMPARATOR)
                .forEach(e -> this.registerBeanPostProcessors((BeanPostProcessor) this.registerBean(e)));
    }

    protected void sortBeanDefinition() {
        synchronized (this.getBeanDefinitions()) {
            Map<String, BeanDefinition> beanDefinitions = this.getBeanDefinitions();
            Map<String, BeanDefinition> sortBeanDefinition = beanDefinitions.values()
                    .stream()
                    .sorted(BEAN_DEFINITION_COMPARATOR)
                    .collect(Collectors.toMap(BeanDefinition::getBeanName, Function.identity(), (k1, k2) -> {
                        throw new IllegalStateException("duplicate key " + k2);
                    }, LinkedHashMap::new));
            beanDefinitions.clear();
            beanDefinitions.putAll(sortBeanDefinition);
        }
    }

    protected void instantiateBeanDefinition() {
        this.sortBeanDefinition();
        for (BeanDefinition beanDefinition : this.getBeanDefinitions().values()) {
            if(beanDefinition.isSingleton()) {
                this.registerBean(beanDefinition);
            }
        }
    }

    protected void invokeContextRefreshed() {
        this.getBeanOfType(ContextAfterRefreshed.class).values().forEach(e -> e.onAfterRefreshed(this));
        this.getBeanOfType(ContextRefreshCompleted.class).values().forEach(e -> e.onCompleted(this));
    }
}
