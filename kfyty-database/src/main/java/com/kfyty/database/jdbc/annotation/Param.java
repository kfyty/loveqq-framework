package com.kfyty.database.jdbc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 表示方法参数名称，可省略
 *
 * @author kfyty725
 * @date 2021/5/22 13:13
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    /**
     * 参数方法名称
     *
     * @return parameter name
     */
    String value() default "";
}
