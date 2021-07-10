package com.kfyty.boot.context;

import com.kfyty.boot.context.factory.AbstractAutowiredBeanFactory;
import com.kfyty.support.annotation.AnnotationWrapper;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.BeanPostProcessor;
import com.kfyty.support.autoconfig.ContextAfterRefreshed;
import com.kfyty.support.autoconfig.ContextRefreshCompleted;
import com.kfyty.support.autoconfig.DestroyBean;
import com.kfyty.support.autoconfig.ImportBeanDefine;
import com.kfyty.support.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.ComponentFilter;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.FactoryBeanDefinition;
import com.kfyty.support.autoconfig.beans.GenericBeanDefinition;
import com.kfyty.support.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.support.event.ApplicationEvent;
import com.kfyty.support.event.ApplicationEventPublisher;
import com.kfyty.support.event.ApplicationListener;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.ReflectUtil;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    protected String[] commanderArgs;
    protected Class<?> primarySource;
    protected Set<Class<?>> scanClasses;
    protected Set<String> excludeBeanNames = new HashSet<>(4);
    protected Set<Class<?>> excludeBeanClasses = new HashSet<>(4);
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
        this.autowiredCapableSupport.doAutowiredLazy();
    }

    protected void registerDefaultBean() {
        this.registerBean(ApplicationContext.class, this);
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

                /* 导入自定义的 bean 定义，可能会被配置排除 */
                this.processImportBeanDefinition(this.scanClasses);

                /* 实例化 bean 定义 */
                this.instantiateBeanDefinition();

                /* 子类扩展 */
                this.onRefresh();

                /* 子类扩展 */
                this.afterRefresh();

                /* 回调 Context 刷新后的接口 */
                this.invokeContextRefreshed();

                /* 添加销毁钩子 */
                Runtime.getRuntime().addShutdownHook(new Thread(this::processDestroy));

                return this;
            } catch (Throwable throwable) {
                log.error("k-boot started failed !");
                try {
                    this.processDestroy();
                } catch (Throwable nestedThrowable) {
                    log.error("process destroy error !", nestedThrowable);
                }
                throw throwable;
            }
        }
    }

    @Override
    public void publishEvent(ApplicationEvent<?> event) {
        this.applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void registerEventListener(ApplicationListener<?> applicationListener) {
        this.applicationEventPublisher.registerEventListener(applicationListener);
    }

    public boolean isExclude(String beanName) {
        return excludeBeanNames.contains(beanName);
    }

    public boolean isExclude(Class<?> beanClass) {
        return excludeBeanClasses.contains(beanClass);
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
    public boolean matchComponentFilter(Class<?> beanClass) {
        Pair<Boolean, AnnotationWrapper<ComponentFilter>> exclude = matchComponentFilter(this.excludeFilterAnnotations, beanClass, false);
        if(!exclude.getKey() && exclude.getValue() != null) {
            return !matchComponentFilter(exclude.getValue().getDeclaring());
        }
        Pair<Boolean, AnnotationWrapper<ComponentFilter>> include = matchComponentFilter(this.includeFilterAnnotations, beanClass, true);
        return include.getKey();
    }

    /**
     * 根据组件过滤器进行匹配
     * @param componentFilterWrappers 组件过滤条件
     * @param beanClass 目标 bean class
     * @param isInclude 当前匹配排除还是包含
     * @return 匹配结果，以及对应的过滤组件
     */
    protected Pair<Boolean, AnnotationWrapper<ComponentFilter>> matchComponentFilter(List<AnnotationWrapper<ComponentFilter>> componentFilterWrappers, Class<?> beanClass, boolean isInclude) {
        for (AnnotationWrapper<ComponentFilter> componentFilterWrapper : componentFilterWrappers) {
            ComponentFilter componentFilter = componentFilterWrapper.get();
            if(Arrays.stream(componentFilter.value()).anyMatch(beanClass.getName()::startsWith)) {
                return new Pair<>(isInclude, componentFilterWrapper);
            }
            if(Arrays.asList(componentFilter.classes()).contains(beanClass)) {
                return new Pair<>(isInclude, componentFilterWrapper);
            }
            if(Arrays.stream(componentFilter.annotations()).anyMatch(e -> AnnotationUtil.hasAnnotation(beanClass, e))) {
                return new Pair<>(isInclude, componentFilterWrapper);
            }
        }
        return new Pair<>(!isInclude, null);
    }

    protected void prepareBeanDefines(Set<Class<?>> scanClasses) {
        scanClasses.stream().filter(e -> !ReflectUtil.isAbstract(e) && this.matchComponentFilter(e)).map(GenericBeanDefinition::from).forEach(this::registerBeanDefinition);
        this.getBeanDefinitions().values().removeIf(this::excludeBeanDefinition);
    }

    protected void processImportBeanDefinition(Set<Class<?>> scanClasses) {
        Set<BeanDefinition> importBeanDefines = this.getBeanDefinitions().values().stream().filter(e -> ImportBeanDefine.class.isAssignableFrom(e.getBeanType())).sorted(Comparator.comparing(BeanUtil::getBeanOrder)).collect(Collectors.toCollection(LinkedHashSet::new));
        for (BeanDefinition importBeanDefine : importBeanDefines) {
            ImportBeanDefine bean = (ImportBeanDefine) this.registerBean(importBeanDefine);
            bean.doImport(scanClasses).stream().filter(e -> !this.excludeBeanDefinition(e)).forEach(this::registerBeanDefinition);
        }
    }

    protected boolean excludeBeanDefinition(BeanDefinition beanDefinition) {
        boolean isExclude = this.isExclude(beanDefinition.getBeanName()) || this.isExclude(beanDefinition.getBeanType());
        if(!isExclude && (beanDefinition instanceof MethodBeanDefinition)) {
            BeanDefinition parentDefinition = ((MethodBeanDefinition) beanDefinition).getParentDefinition();
            isExclude = this.isExclude(parentDefinition.getBeanName()) || this.isExclude(parentDefinition.getBeanType());
        }
        if(!isExclude && (beanDefinition instanceof FactoryBeanDefinition)) {
            BeanDefinition factoryBeanDefinition = ((FactoryBeanDefinition) beanDefinition).getFactoryBeanDefinition();
            isExclude = this.isExclude(factoryBeanDefinition.getBeanName()) || this.isExclude(factoryBeanDefinition.getBeanType());
        }
        if(isExclude) {
            log.info("exclude bean definition: {}", beanDefinition);
        }
        return isExclude;
    }

    protected void registerBeanPostProcessors() {
        this.getBeanDefinitions().values()
                .stream()
                .filter(e -> BeanPostProcessor.class.isAssignableFrom(e.getBeanType()))
                .sorted(Comparator.comparing(e -> InstantiationAwareBeanPostProcessor.class.isAssignableFrom(((BeanDefinition) e).getBeanType()) ? Order.HIGHEST_PRECEDENCE : Order.LOWEST_PRECEDENCE).thenComparing(e -> BeanUtil.getBeanOrder((BeanDefinition) e)))
                .forEach(e -> this.registerBeanPostProcessors((BeanPostProcessor) this.registerBean(e)));
    }

    protected void sortBeanDefinition() {
        Map<String, BeanDefinition> beanDefinitions = this.getBeanDefinitions();
        Map<String, BeanDefinition> sortBeanDefinition = beanDefinitions.values()
                .stream()
                .sorted(Comparator.comparing(e -> InstantiationAwareBeanPostProcessor.class.isAssignableFrom(((BeanDefinition) e).getBeanType()) ? Order.HIGHEST_PRECEDENCE : Order.LOWEST_PRECEDENCE).thenComparing(e -> BeanUtil.getBeanOrder((BeanDefinition) e)))
                .collect(Collectors.toMap(BeanDefinition::getBeanName, Function.identity(), (k1, k2) -> {
                    throw new IllegalStateException("duplicate key " + k2);
                }, LinkedHashMap::new));
        synchronized (this.getBeanDefinitions()) {
            beanDefinitions.clear();
            beanDefinitions.putAll(sortBeanDefinition);
        }
    }

    protected void instantiateBeanDefinition() {
        this.sortBeanDefinition();
        for (BeanDefinition beanDefinition : this.getBeanDefinitions().values()) {
            this.registerBean(beanDefinition);
        }
    }

    protected void invokeContextRefreshed() {
        this.getBeanOfType(ContextAfterRefreshed.class).values().forEach(e -> e.onAfterRefreshed(this));
        this.getBeanOfType(ContextRefreshCompleted.class).values().forEach(e -> e.onCompleted(this));
    }

    protected void processDestroy() {
        log.info("destroy bean...");

        this.forEach((beanName, bean) -> {

            this.getBeanPostProcessors().forEach(e -> e.postProcessBeforeDestroy(bean, beanName));

            if(bean instanceof DestroyBean) {
                ((DestroyBean) bean).onDestroy();
            }

            BeanDefinition beanDefinition = this.getBeanDefinition(beanName);
            if(beanDefinition instanceof MethodBeanDefinition) {
                MethodBeanDefinition methodBeanDefinition = (MethodBeanDefinition) beanDefinition;
                Optional.ofNullable(methodBeanDefinition.getDestroyMethod(this)).ifPresent(e -> ReflectUtil.invokeMethod(bean, e));
            }
        });
    }
}
