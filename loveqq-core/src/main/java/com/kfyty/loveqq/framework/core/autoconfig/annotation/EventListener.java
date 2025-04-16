package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import com.kfyty.loveqq.framework.core.event.ApplicationEvent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 事件监听器注解
 * <b>
 * 仅在类上同时注释时，方法注释才有效
 * </b>
 *
 * @author kfyty725
 * @date 2021/6/21 16:45
 * @email kfyty725@hotmail.com
 * @see com.kfyty.loveqq.framework.core.event.EventListenerAdapter
 * @see com.kfyty.loveqq.framework.boot.event.EventListenerRegistry
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface EventListener {
    /**
     * 要监听的事件
     */
    Class<? extends ApplicationEvent<?>>[] value() default {};

    /**
     * 监听器监听条件，条件存在时，仅当条件表达式结果为 true 时才会被调用
     *
     * @return ognl 表达式
     */
    String condition() default "";
}
