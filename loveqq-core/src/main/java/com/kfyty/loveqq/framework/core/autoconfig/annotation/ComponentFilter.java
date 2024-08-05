package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 用于过滤需要生成 bean 定义的 Class
 * <p>
 * 一般和 {@link ComponentScan} 结合使用
 *
 * @author kfyty725
 * @date 2021/5/21 16:46
 * @email kfyty725@hotmail.com
 * @see ConfigurableApplicationContext#isMatchComponent(Class)
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
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
