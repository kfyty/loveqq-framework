package com.kfyty.mvc.annotation;

import com.kfyty.core.autoconfig.annotation.Component;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 全局异常处理
 *
 * @see Controller
 * @see RestControllerAdvice
 * @see com.kfyty.mvc.autoconfig.ControllerAdviceBeanPostProcessor
 */
@Component
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerAdvice {

    String value() default "";

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    Class<? extends Annotation>[] annotations() default {};
}
