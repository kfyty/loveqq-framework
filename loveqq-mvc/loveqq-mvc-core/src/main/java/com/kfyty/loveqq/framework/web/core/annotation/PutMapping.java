package com.kfyty.loveqq.framework.web.core.annotation;

import com.kfyty.loveqq.framework.core.lang.annotation.AliasFor;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;

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
@RequestMapping(method = RequestMethod.PUT)
public @interface PutMapping {
    @AliasFor(annotation = RequestMapping.class)
    String value() default "";

    @AliasFor(annotation = RequestMapping.class)
    String produces() default "text/plain; charset=utf-8";

    @AliasFor(annotation = RequestMapping.class)
    RequestMapping.DefaultMapping defaultMapping() default RequestMapping.DefaultMapping.DEFAULT;
}
