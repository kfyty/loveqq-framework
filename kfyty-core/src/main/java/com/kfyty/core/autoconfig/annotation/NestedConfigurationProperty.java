package com.kfyty.core.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 标记是一个嵌套的配置属性
 *
 * @author kfyty725
 * @date 2022/5/25 22:44
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface NestedConfigurationProperty {
}
