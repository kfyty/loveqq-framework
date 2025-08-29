package com.kfyty.loveqq.framework.boot.test;

import com.kfyty.loveqq.framework.boot.context.DefaultConfigurableApplicationContext;
import com.kfyty.loveqq.framework.boot.test.annotation.LoveqqTest;
import com.kfyty.loveqq.framework.core.autoconfig.SerialInitialize;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * 描述: loveqq 集成 junit5
 *
 * @author kfyty725
 * @date 2021/7/29 13:07
 * @email kfyty725@hotmail.com
 */
public class LoveqqExtension extends DefaultConfigurableApplicationContext implements BeforeAllCallback, AfterAllCallback, TestInstanceFactory {
    /**
     * log
     */
    protected Logger log;

    /**
     * 测试注解
     */
    protected LoveqqTest annotation;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        this.log = LoggerFactory.getLogger(testClass);
        this.annotation = testClass.getAnnotation(LoveqqTest.class);
        Class<?> primarySource = this.annotation.value() == Object.class ? testClass : this.annotation.value();
        this.refreshApplicationContext(testClass, primarySource);
    }

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext) throws TestInstantiationException {
        String beanName = BeanDefinitionBuilder.resolveBeanName(factoryContext.getTestClass());
        return this.applicationContext.getBean(beanName);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        this.applicationContext.close();
    }

    @Override
    protected void invokeBeanFactoryPostProcessor() {
        for (String property : this.annotation.properties()) {
            this.propertiesContext.addConfig(property);
            this.propertiesContext.loadProperties(property);
        }
        super.invokeBeanFactoryPostProcessor();
    }

    @Override
    protected void finishBeanFactoryInitialization() {
        // 单元测试全局懒加载，仅初始化串行初始化的 bean，避免实例化不必要的 bean
        for (BeanDefinition value : this.getBeanDefinitions(SerialInitialize.class).values()) {
            if (value.isSingleton() && value.isAutowireCandidate() && !value.isLazyInit()) {
                this.registerBean(value);
            }
        }
    }

    protected void refreshApplicationContext(Class<?> testClass, Class<?> primarySource) {
        this.setPrimarySource(primarySource);
        this.setCommandLineArgs(this.annotation.args());
        this.addSources(Arrays.asList(this.annotation.classes()));
        if (testClass != primarySource) {
            this.addSource(testClass);
        }

        this.log.info("Test Boot loading...");

        long start = System.currentTimeMillis();

        this.refresh();

        this.log.info("Test Boot loaded succeed in {} seconds", (System.currentTimeMillis() - start) / 1000D);
    }
}
