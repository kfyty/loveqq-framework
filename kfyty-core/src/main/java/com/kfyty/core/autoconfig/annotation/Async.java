package com.kfyty.core.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 用以支持异步任务
 *
 * @author kfyty725
 * @date 2021/6/26 11:03
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Async {
    /**
     * 执行该任务的线程池的 bean name，必须是 ExecutorService 的子类
     */
    String value() default "";
}
