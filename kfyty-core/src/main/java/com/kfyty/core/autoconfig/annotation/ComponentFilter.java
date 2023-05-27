package com.kfyty.core.autoconfig.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 用于过滤需要生成 bean 定义的 Class
 * 一般和 ComponentScan 结合使用，单独使用时，等价为 ${@link ComponentScan#includeFilter()}
 *
 * @author kfyty725
 * @date 2021/5/21 16:46
 * @email kfyty725@hotmail.com
 * @see com.kfyty.core.autoconfig.ConfigurableApplicationContext#doFilterComponent(Class)
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentFilter {
    /**
     * 基础包名
     */
    String[] value() default {};

    /**
     * 具体的某些类
     */
    Class<?>[] classes() default {};

    /**
     * 某些注解存在
     */
    Class<? extends Annotation>[] annotations() default {};
}
