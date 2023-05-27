package com.kfyty.core.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 自动绑定配置属性
 *
 * @author kfyty725
 * @date 2022/5/25 22:36
 * @email kfyty725@hotmail.com
 * @see com.kfyty.core.autoconfig.env.DataBinder
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ConfigurationProperties {
    /**
     * 属性前缀
     *
     * @return prefix
     */
    String value();

    /**
     * 配置文件中的值无法转换为目标字段类型时是否忽略
     *
     * @return 默认 false
     */
    boolean ignoreInvalidFields() default false;

    /**
     * 目标字段在配置文件中不存在时是否忽略
     *
     * @return 默认 true
     */
    boolean ignoreUnknownFields() default true;
}
