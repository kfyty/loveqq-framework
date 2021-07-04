package com.kfyty.support.autoconfig.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 描述: 用于过滤需要生成 bean 定义的 Class
 *
 * @author kfyty725
 * @date 2021/5/21 16:46
 * @email kfyty725@hotmail.com
 */
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
     * 注解
     */
    Class<? extends Annotation>[] annotations() default {};
}
