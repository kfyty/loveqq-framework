package com.kfyty.mvc.annotation;

import com.kfyty.mvc.request.RequestMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 请求映射路径
 *
 * @see RequestMapping
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(value = "", requestMethod = RequestMethod.GET)
public @interface GetMapping {

    String value() default "";

    String produces() default "";
}
