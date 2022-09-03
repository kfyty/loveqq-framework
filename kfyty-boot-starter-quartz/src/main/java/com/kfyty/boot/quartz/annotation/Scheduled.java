package com.kfyty.boot.quartz.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 描述: 定时任务注解
 *
 * @author kfyty725
 * @date 2021/10/17 20:01
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Scheduled {
    /**
     * 运行该任务的 cron 表达式
     */
    String cron() default "";

    /**
     * 时区
     *
     * @return 时区
     */
    String zone() default "";

    /**
     * 首次执行时延迟的时间
     *
     * @return 时间
     */
    long initialDelay() default -1;

    /**
     * 上一次执行结束至下一次执行开始应间隔的时间
     *
     * @return 间隔
     */
    long fixedDelay() default -1;

    /**
     * 两次调用间隔的周期
     *
     * @return 周期
     */
    long fixedRate() default -1;

    /**
     * 时间单位
     *
     * @return 单位
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
