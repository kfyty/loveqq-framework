package com.kfyty.boot.quartz.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 定时任务注解
 *
 * @author kfyty725
 * @date 2021/10/17 20:01
 * @email kfyty725@hotmail.com
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scheduled {
    /**
     * 运行该任务的 cron 表达式
     */
    String cron();
}
