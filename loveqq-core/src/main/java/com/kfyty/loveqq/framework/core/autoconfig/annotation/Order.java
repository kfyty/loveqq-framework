package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 标记优先级
 *
 * @author kfyty725
 * @date 2021/6/13 11:28
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Order {

    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE + 1;

    int LOWEST_PRECEDENCE = Integer.MAX_VALUE - 1;

    int value() default LOWEST_PRECEDENCE >> 1;
}
