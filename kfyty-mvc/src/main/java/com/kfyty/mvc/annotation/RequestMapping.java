package com.kfyty.mvc.annotation;

import com.kfyty.mvc.request.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequestMapping {
    /**
     * 请求映射路径
     */
    String value();

    /**
     * 请求方法
     * @return 默认 GET 方法
     */
    RequestMethod requestMethod() default RequestMethod.GET;
}
