package com.kfyty.boot.xxl.job.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: {@link com.xxl.job.core.handler.annotation.XxlJob} 的替代
 * 支持注解在 class 级别，用于标识是一个 job 类，避免 ioc 懒加载失效
 *
 * @author kfyty725
 * @date 2023/3/28 18:49
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface XxlJob {
    /**
     * job handler name
     */
    String value() default "";

    /**
     * init handler, invoked when JobThread init
     */
    String init() default "";

    /**
     * destroy handler, invoked when JobThread destroy
     */
    String destroy() default "";
}
