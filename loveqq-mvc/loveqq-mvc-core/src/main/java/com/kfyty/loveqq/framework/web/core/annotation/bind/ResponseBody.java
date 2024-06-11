package com.kfyty.loveqq.framework.web.core.annotation.bind;

import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 默认返回 json 数据
 * 可提供 {@link HandlerMethodReturnValueProcessor} 进行覆盖
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ResponseBody {
    /**
     * 响应体类型
     */
    String value() default "application/json; charset=utf-8";
}
