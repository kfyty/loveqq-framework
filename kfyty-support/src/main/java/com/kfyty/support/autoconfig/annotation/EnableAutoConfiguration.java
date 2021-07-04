package com.kfyty.support.autoconfig.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 自动配置
 *
 * @author kfyty725
 * @date 2021/6/12 11:28
 * @email kfyty725@hotmail.com
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableAutoConfiguration {

    Class<?>[] exclude() default {};

    String[] excludeNames() default {};

    ComponentFilter includeFilter() default @ComponentFilter(annotations = {
            BootApplication.class, Configuration.class, Component.class, Service.class, Repository.class
    });

    ComponentFilter excludeFilter() default @ComponentFilter();
}
