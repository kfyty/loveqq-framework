package com.kfyty.loveqq.framework.data.cache.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 描述: 添加缓存注解
 * <b>
 * 注意：sse 事件流禁止添加缓存注解
 * </b>
 *
 * @author kfyty725
 * @date 2024/7/4 9:52
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {
    /**
     * 缓存名称，可使用 ognl 表达式，注意：此时表达式的 retVal 属性不可用
     *
     * @return cache name
     */
    String value() default "";

    /**
     * 放入缓存的条件，可使用 ognl 表达式
     *
     * @return 条件表达式
     */
    String condition() default "";

    /**
     * 超时时间
     *
     * @return ttl
     */
    long ttl() default 0;

    /**
     * 时间单位
     *
     * @return time unit
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 返回 null 时，是否放入缓存
     * <b>响应式缓存不支持此属性</b>
     *
     * @return true/false
     */
    boolean putIfNull() default false;
}
