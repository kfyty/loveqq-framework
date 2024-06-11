package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 启动类注解
 *
 * @author kfyty725
 * @date 2021/6/12 11:28
 * @email kfyty725@hotmail.com
 */
@Documented
@Configuration
@EnableAutoConfiguration
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BootApplication {

    String value() default "";

    Class<?>[] exclude() default {};

    String[] excludeNames() default {};

    boolean proxyTargetClass() default false;
}
