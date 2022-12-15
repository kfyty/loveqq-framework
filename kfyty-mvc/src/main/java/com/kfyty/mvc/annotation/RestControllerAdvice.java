package com.kfyty.mvc.annotation;

import com.kfyty.mvc.annotation.bind.ResponseBody;

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
 * @see ResponseBody
 * @see ControllerAdvice
 * @see com.kfyty.mvc.autoconfig.ControllerAdviceBeanPostProcessor
 */
@Documented
@ResponseBody
@ControllerAdvice
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestControllerAdvice {

    String value() default "";

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    Class<? extends Annotation>[] annotations() default {};
}
