package com.kfyty.loveqq.framework.web.core.annotation;

import com.kfyty.loveqq.framework.web.core.request.RequestMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 请求映射路径
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequestMapping {
    /**
     * 默认的 content-type
     */
    String DEFAULT_PRODUCES = "text/plain; charset=utf-8";

    /**
     * 请求映射路径
     * 默认为方法名称
     */
    String value() default "";

    /**
     * 请求方法
     *
     * @return 默认 GET 方法
     */
    RequestMethod method() default RequestMethod.GET;

    /**
     * 设置响应的 content-type
     *
     * @return content-type
     */
    String produces() default DEFAULT_PRODUCES;

    /**
     * {@link this#value()} 为空时的处理方式
     *
     * @return {@link Strategy.DEFAULT}
     * @see Strategy#EMPTY 空字符串
     * @see Strategy#DEFAULT 方法名称
     */
    Strategy strategy() default Strategy.DEFAULT;

    enum Strategy {
        EMPTY,
        DEFAULT
    }
}
