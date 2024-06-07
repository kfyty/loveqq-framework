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
@RequestMapping(requestMethod = RequestMethod.PATCH)
public @interface PatchMapping {

    String value() default "";

    String produces() default "text/plain; charset=utf-8";

    RequestMapping.DefaultMapping defaultMapping() default RequestMapping.DefaultMapping.DEFAULT;
}
