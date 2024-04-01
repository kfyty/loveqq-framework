package com.kfyty.web.mvc.core.annotation;

import com.kfyty.web.mvc.core.request.RequestMethod;

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
@RequestMapping(requestMethod = RequestMethod.PUT)
public @interface PutMapping {

    String value() default "";

    String produces() default "";
}
