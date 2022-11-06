package com.kfyty.boot.context;

import com.kfyty.boot.context.factory.AbstractAutowiredBeanFactory;
import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.core.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.core.autoconfig.BeanFactoryPreProcessor;
import com.kfyty.core.autoconfig.BeanPostProcessor;
import com.kfyty.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.event.ApplicationEvent;
import com.kfyty.core.event.ApplicationEventPublisher;
import com.kfyty.core.event.ApplicationListener;
import com.kfyty.core.event.ContextRefreshedEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 描述: 上下文基础实现
 *
 * @author kfyty725
 * @date 2021/7/3 11:05
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class AbstractApplicationContext extends AbstractAutowiredBeanFactory implements ApplicationContext {
    protected final Thread shutdownHook = new Thread(this::close);

    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = this;
    }

    @Override
    public ApplicationContext refresh() {
        synchronized (this) {
            try {
                /* 刷新前的准备，由子类扩展 */
                this.beforeRefresh();

                /* 执行 bean 工厂前置处理器 */
                this.invokeBeanFactoryPreProcessor();

                /* 执行 bean 工厂后置处理器 */
                this.invokeBeanFactoryPostProcessor();

                /* 注册 bean 后置处理器 */
                this.registerBeanPostProcessors();

                /* 子类扩展 */
                this.onRefresh();

                /* 实例化单例 bean 定义 */
                this.finishBeanFactoryInitialization();

                /* 子类扩展 */
                this.afterRefresh();

                /* 结束刷新 */
                this.finishRefresh();

                return this;
            } catch (Throwable throwable) {
                log.error("k-boot started failed !");
                try {
                    this.close();
                } catch (Throwable nestedThrowable) {
                    log.error("close application context error !", nestedThrowable);
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

    @Override
    public void close() {
        super.close();
        this.applicationEventPublisher = null;
    }

    @Override
    protected void invokeAwareMethod(String beanName, Object bean) {
        super.invokeAwareMethod(beanName, bean);
        if (bean instanceof ApplicationContextAware) {
            ((ApplicationContextAware) bean).setApplicationContext(this);
        }
    }

    protected void registerDefaultBean() {
        this.registerBean(BeanFactory.class, this);
        this.registerBean(ApplicationContext.class, this);
    }

    protected void beforeRefresh() {
        this.close();
        this.registerDefaultBean();
        Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
    }

    protected void onRefresh() {

    }

    protected void afterRefresh() {
        this.autowiredLazied();
        for (ContextAfterRefreshed contextAfterRefreshed : this.getBeanOfType(ContextAfterRefreshed.class).values()) {
            contextAfterRefreshed.onAfterRefreshed(this);
        }
    }

    protected void finishRefresh() {
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        this.publishEvent(new ContextRefreshedEvent(this));
    }

    protected void invokeBeanFactoryPreProcessor() {
        Map<String, BeanDefinition> beanFactoryPreProcessors = this.getBeanDefinitions(BeanFactoryPreProcessor.class);
        for (BeanDefinition beanDefinition : beanFactoryPreProcessors.values()) {
            BeanFactoryPreProcessor beanFactoryPreProcessor = (BeanFactoryPreProcessor) this.registerBean(beanDefinition);
            beanFactoryPreProcessor.preProcessBeanFactory(this);
        }
    }

    protected void invokeBeanFactoryPostProcessor() {
        Map<String, BeanDefinition> beanFactoryPostProcessors = this.getBeanDefinitions(BeanFactoryPostProcessor.class);
        for (BeanDefinition beanDefinition : beanFactoryPostProcessors.values()) {
            BeanFactoryPostProcessor beanFactoryPostProcessor = (BeanFactoryPostProcessor) this.registerBean(beanDefinition);
            beanFactoryPostProcessor.postProcessBeanFactory(this);
        }
        this.resolveConditionBeanDefinitionRegistry();
    }

    protected void registerBeanPostProcessors() {
        Map<String, BeanDefinition> beanPostProcessors = this.getBeanDefinitions(BeanPostProcessor.class);
        for (BeanDefinition beanDefinition : beanPostProcessors.values()) {
            this.registerBeanPostProcessors(beanDefinition.getBeanName(), (BeanPostProcessor) this.registerBean(beanDefinition));
        }
    }

    protected void finishBeanFactoryInitialization() {
        this.sortBeanDefinition();
        for (BeanDefinition beanDefinition : this.getBeanDefinitions().values()) {
            if (beanDefinition.isSingleton() && !beanDefinition.isLazyInit()) {
                this.registerBean(beanDefinition);
            }
        }
    }

    protected void sortBeanDefinition() {
        synchronized (this.getBeanDefinitions()) {
            Map<String, BeanDefinition> sortBeanDefinition = this.getBeanDefinitions(e -> true);
            beanDefinitions.clear();
            beanDefinitions.putAll(sortBeanDefinition);
        }
    }
}
