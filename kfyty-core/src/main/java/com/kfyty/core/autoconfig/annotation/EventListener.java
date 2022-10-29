package com.kfyty.core.autoconfig.annotation;

import com.kfyty.core.event.ApplicationEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 事件监听器注解
 *
 * @author kfyty725
 * @date 2021/6/21 16:45
 * @email kfyty725@hotmail.com
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventListener {
    /**
     * 要监听的事件
     */
    Class<? extends ApplicationEvent<?>>[] value() default {};
}
