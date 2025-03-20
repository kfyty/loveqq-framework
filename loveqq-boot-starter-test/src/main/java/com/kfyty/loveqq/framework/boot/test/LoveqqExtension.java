package com.kfyty.loveqq.framework.boot.test;

import com.kfyty.loveqq.framework.boot.context.DefaultConfigurableApplicationContext;
import com.kfyty.loveqq.framework.boot.test.annotation.LoveqqTest;
import com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder;
import com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;

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
     * 测试注解
     */
    protected LoveqqTest annotation;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        LoveqqTest annotation = testClass.getAnnotation(LoveqqTest.class);
        Class<?> primarySource = annotation.value() == Object.class ? testClass : annotation.value();
        this.refreshApplicationContext(primarySource, annotation);
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
    public void onRefresh() {
        PropertyContext propertyContext = this.getBean(PropertyContext.class);
        for (String property : this.annotation.properties()) {
            propertyContext.addConfig(property);
            propertyContext.loadProperties(property);
        }
    }

    protected void refreshApplicationContext(Class<?> primarySource, LoveqqTest annotation) {
        this.annotation = annotation;
        this.setPrimarySource(primarySource);
        this.setCommandLineArgs(annotation.args());
        this.addScannedClasses(Arrays.asList(annotation.classes()));
        this.refresh();
    }
}
