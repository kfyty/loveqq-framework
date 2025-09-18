package com.kfyty.mvc.annotation;

import com.kfyty.core.autoconfig.annotation.Component;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 全局异常处理
 *
 * @author kfyty725
 * @date 2021/6/18 10:15
 * @email kfyty725@hotmail.com
 */
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerAdvice {

    String value() default "";

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    Class<? extends Annotation>[] annotations() default {};
}
