package com.kfyty.support.autoconfig.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 标记该类为一个 bean 定义，同时可以排除一些自动配置类，加载自定义注解
 *
 * @author kfyty725
 * @date 2021/6/12 11:28
 * @email kfyty725@hotmail.com
 */
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BootApplication {

    String value() default "";

    Class<?>[] exclude() default {};

    String[] excludeNames() default {};

    boolean proxyTargetClass() default true;

    ComponentFilter componentFilter() default @ComponentFilter(includeFilter = {
            BootApplication.class, Configuration.class, Component.class, Service.class, Repository.class
    });
}
