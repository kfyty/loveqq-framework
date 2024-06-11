package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 读取配置文件的值
 *
 * @author kfyty725
 * @date 2022/3/12 14:58
 * @email kfyty725@hotmail.com
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {
    /**
     * 配置文件的属性 eg: ${a.b:default}
     */
    String value();
}
