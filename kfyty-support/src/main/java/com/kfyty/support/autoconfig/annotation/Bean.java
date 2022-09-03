package com.kfyty.support.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 标记该方法为一个 bean 定义
 *
 * @author kfyty725
 * @date 2021/6/12 11:28
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
    /**
     * bean name
     *
     * @return 默认为方法名
     */
    String value() default "";

    /**
     * bean 初始化方法
     *
     * @return 初始化方法
     */
    String initMethod() default "";

    /**
     * 销毁方法
     *
     * @return 销毁方法
     */
    String destroyMethod() default "";
}
