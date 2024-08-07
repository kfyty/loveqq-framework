package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 单例中注入原型实例可使用
 * <b>
 * 仅在类上同时注释时，方法注释才有效
 * </b>
 *
 * @author kfyty725
 * @date 2021/7/11 12:29
 * @email kfyty725@hotmail.com
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Lookup {
    /**
     * bean 名称，默认解析返回值类型
     */
    String value() default "";
}
