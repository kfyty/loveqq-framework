package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.beans.AutowiredCapableSupport;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.support.io.PathMatchingResourcePatternResolver;
import lombok.Getter;

import java.util.function.Supplier;

/**
 * 描述: ioc 便捷操作
 *
 * @author kfyty725
 * @date 2023/4/17 14:27
 * @email kfyty725@hotmail.com
 */
public abstract class IOC {
    /**
     * true if servlet based web server
     */
    private static volatile Boolean isServletWeb;

    /**
     * bean factory
     */
    @Getter
    private static volatile BeanFactory beanFactory;

    /**
     * 设置 bean 工厂
     *
     * @param beanFactory bean 工厂
     */
    public static void setBeanFactory(BeanFactory beanFactory) {
        IOC.beanFactory = beanFactory;
    }

    /**
     * whether servlet based web server
     *
     * @return true if servlet based web server
     */
    public static boolean isServletWeb() {
        if (isServletWeb == null) {
            isServletWeb = ReflectUtil.isPresent("com.kfyty.loveqq.framework.web.mvc.servlet.ServletWebServer");
            if (isServletWeb) {
                Class<?> servletServerClass = ReflectUtil.load("com.kfyty.loveqq.framework.web.mvc.servlet.ServletWebServer", false, false);
                isServletWeb = servletServerClass != null && !beanFactory.getBeanDefinitions(servletServerClass).isEmpty();
            }
        }
        return isServletWeb;
    }

    /**
     * 获取 bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return beanFactory.getBean(clazz);
    }

    /**
     * 获取 bean
     */
    public static <T> T getBean(String name) {
        return beanFactory.getBean(name);
    }

    /**
     * 获取属性配置
     *
     * @param key   配置 key
     * @param clazz 要转换的数据类型
     * @return 配置
     */
    public static <T> T getProperty(String key, Class<T> clazz) {
        return getPropertyContext().getProperty(key, clazz);
    }

    /**
     * 获取配置文件上下文
     *
     * @return 配置文件上下文
     */
    public static GenericPropertiesContext getPropertyContext() {
        return getBean(GenericPropertiesContext.class);
    }

    /**
     * 获取资源解析器
     *
     * @return 资源解析器
     */
    public static PathMatchingResourcePatternResolver getResourceResolver() {
        return getBean(PathMatchingResourcePatternResolver.class);
    }

    /**
     * 获取自动注入能力支持，可用于手动注入依赖
     *
     * @return 自动注入能力支持
     */
    public static AutowiredCapableSupport getAutowiredCapable() {
        return getBean(AutowiredCapableSupport.BEAN_NAME);
    }

    /**
     * 获取应用上下文
     *
     * @return 应用上下文
     */
    public static ApplicationContext getApplicationContext() {
        if (beanFactory instanceof ApplicationContext) {
            return (ApplicationContext) beanFactory;
        }
        throw new IllegalStateException("The bean factory doesn't instance of ApplicationContext.");
    }

    /**
     * 获取可配置的应用上下文
     *
     * @return 可配置的应用上下文
     */
    public static ConfigurableApplicationContext getConfigurableApplicationContext() {
        if (beanFactory instanceof ConfigurableApplicationContext) {
            return (ConfigurableApplicationContext) beanFactory;
        }
        throw new IllegalStateException("The bean factory doesn't instance of ConfigurableApplicationContext.");
    }

    /**
     * 发布事件
     *
     * @param event 事件
     */
    public static void publishEvent(ApplicationEvent<?> event) {
        getApplicationContext().publishEvent(event);
    }

    public static <T> T newInstance(Supplier<T> provider) {
        T instance = provider.get();
        getAutowiredCapable().autowiredBean(null, instance);
        return instance;
    }
}
