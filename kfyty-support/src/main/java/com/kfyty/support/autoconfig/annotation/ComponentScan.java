package com.kfyty.support.autoconfig.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 用于添加需要扫描的基础包名
 *
 * @author kfyty725
 * @date 2021/5/21 16:46
 * @email kfyty725@hotmail.com
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentScan {
    String[] value() default {};
}
