package com.kfyty.loveqq.framework.boot.context;

import com.kfyty.loveqq.framework.boot.context.factory.AbstractAutowiredBeanFactory;
import com.kfyty.loveqq.framework.boot.processor.factory.internal.HardCodeBeanFactoryPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPreProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.BeanPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.loveqq.framework.core.autoconfig.ContextOnRefresh;
import com.kfyty.loveqq.framework.core.autoconfig.SerializableInitialize;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Lazy;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.aware.PropertyContextContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinitionRegistry;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.beans.ConditionBeanDefinitionRegistry;
import com.kfyty.loveqq.framework.core.autoconfig.boostrap.Bootstrap;
import com.kfyty.loveqq.framework.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.event.ApplicationEventPublisher;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.event.ContextRefreshedEvent;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.core.lang.JarIndexClassLoader;
import com.kfyty.loveqq.framework.core.utils.CompletableFutureUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.kfyty.loveqq.framework.boot.autoconfig.ThreadPoolExecutorAutoConfig.DEFAULT_THREAD_POOL_EXECUTOR;

/**
 * 描述: 上下文基础实现
 *
 * @author kfyty725
 * @date 2021/7/3 11:05
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class AbstractApplicationContext extends AbstractAutowiredBeanFactory implements ApplicationContext {
    /**
     * 销毁回调
     */
    protected final Thread shutdownHook = new Thread(this::close, "LoveqqFrameworkShutdownHook");

    /**
     * 属性上下文
     */
    @Autowired
    protected GenericPropertiesContext propertiesContext;

    /**
     * 默认线程池
     */
    @Lazy
    @Autowired(DEFAULT_THREAD_POOL_EXECUTOR)
    protected ExecutorService executorService;

    /**
     * 事件订阅发布器
     */
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
                log.error("loveqq-boot start failed.", throwable);
                try {
                    this.close();
                } catch (Throwable nestedThrowable) {
                    log.error("close application context error.", nestedThrowable);
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
        this.propertiesContext = null;
        this.executorService = null;
        this.applicationEventPublisher = null;
    }

    @Override
    protected void invokeAwareMethod(String beanName, Object bean) {
        super.invokeAwareMethod(beanName, bean);
        if (bean instanceof PropertyContextContextAware) {
            ((PropertyContextContextAware) bean).setPropertyContext(this.propertiesContext);
        }
        if (bean instanceof ApplicationContextAware) {
            ((ApplicationContextAware) bean).setApplicationContext(this);
        }
    }

    protected void registerDefaultBean() {
        this.registerBean(BeanFactory.class, this);
        this.registerBean(BeanDefinitionRegistry.class, this);
        this.registerBean(ConditionBeanDefinitionRegistry.class, this);

        this.registerBean(ApplicationContext.class, this);
        this.registerBean(ConfigurableApplicationContext.class, this);
    }

    protected void beforeRefresh() {
        this.close();
        this.registerDefaultBean();
        Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
    }

    protected void onRefresh() {
        for (ContextOnRefresh contextOnRefresh : this.getBeanOfType(ContextOnRefresh.class).values()) {
            contextOnRefresh.onRefresh(this);
        }
    }

    protected void afterRefresh() {
        for (ContextAfterRefreshed contextAfterRefreshed : this.getBeanOfType(ContextAfterRefreshed.class).values()) {
            contextAfterRefreshed.onAfterRefreshed(this);
        }
    }

    protected void finishRefresh() {
        // 添加回调
        Runtime.getRuntime().addShutdownHook(this.shutdownHook);

        // 关闭 jar file
        if (!this.isBootstrap()) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader instanceof JarIndexClassLoader cl) {
                cl.closeJarFileCache();
            }
        }

        // 发布刷新成功事件
        this.publishEvent(new ContextRefreshedEvent(this));
    }

    protected boolean isBootstrap() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            Class<?> loaded = ReflectUtil.load(element.getClassName());
            if (Bootstrap.class.isAssignableFrom(loaded)) {
                return true;
            }
        }
        return false;
    }

    protected void invokeBeanFactoryPreProcessor() {
        Map<String, BeanDefinition> beanFactoryPreProcessors = this.getBeanDefinitions(BeanFactoryPreProcessor.class);
        for (BeanDefinition beanDefinition : beanFactoryPreProcessors.values()) {
            BeanFactoryPreProcessor beanFactoryPreProcessor = (BeanFactoryPreProcessor) this.registerBean(beanDefinition);
            beanFactoryPreProcessor.preProcessBeanFactory(this);
        }
    }

    protected void invokeBeanFactoryPostProcessor() {
        HardCodeBeanFactoryPostProcessor hardCodeBeanFactoryPostProcessor = null;
        Map<String, BeanDefinition> beanFactoryPostProcessors = this.getBeanDefinitions(BeanFactoryPostProcessor.class);
        for (BeanDefinition beanDefinition : beanFactoryPostProcessors.values()) {
            BeanFactoryPostProcessor beanFactoryPostProcessor = (BeanFactoryPostProcessor) this.registerBean(beanDefinition);
            if (beanFactoryPostProcessor.getClass() == HardCodeBeanFactoryPostProcessor.class) {
                hardCodeBeanFactoryPostProcessor = (HardCodeBeanFactoryPostProcessor) beanFactoryPostProcessor;
            }
            if (beanFactoryPostProcessor instanceof HardCodeBeanFactoryPostProcessor) {
                continue;
            }
            beanFactoryPostProcessor.postProcessBeanFactory(this);
        }
        if (hardCodeBeanFactoryPostProcessor != null) {
            hardCodeBeanFactoryPostProcessor.postProcessBeanFactory(this);
        }
        this.resolveConditionBeanDefinitionRegistry();
        log.info("The bean definition loaded finished.");
    }

    protected void registerBeanPostProcessors() {
        Map<String, BeanDefinition> beanPostProcessors = this.getBeanDefinitions(BeanPostProcessor.class);
        for (BeanDefinition beanDefinition : beanPostProcessors.values()) {
            this.registerBeanPostProcessors(beanDefinition.getBeanName(), (BeanPostProcessor) this.registerBean(beanDefinition));
        }
    }

    protected void finishBeanFactoryInitialization() {
        // 是否全局懒加载
        if (Boolean.parseBoolean(System.getProperty(ConstantConfig.LAZY_INIT_KEY, Boolean.FALSE.toString()))) {
            return;
        }

        // 读取全局配置
        boolean concurrentInitialize = Boolean.parseBoolean(System.getProperty(ConstantConfig.CONCURRENT_INIT_KEY, Boolean.FALSE.toString()));

        // 先实例化串行 bean
        Map<String, BeanDefinition> beanDefinitions = concurrentInitialize ? this.getBeanDefinitions(SerializableInitialize.class) : this.getBeanDefinitions();
        for (BeanDefinition value : beanDefinitions.values()) {
            if (value.isSingleton() && value.isAutowireCandidate() && !value.isLazyInit()) {
                this.registerBean(value);
            }
        }

        // 并发实例化剩余的单例 bean
        if (concurrentInitialize) {
            CompletableFutureUtil.consumer(this.executorService, this.getBeanDefinitions().values(), bd -> {
                if (bd.isSingleton() && bd.isAutowireCandidate() && !bd.isLazyInit()) {
                    this.registerBean(bd);
                }
            });
        }
    }
}
