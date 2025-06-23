package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.env.DataBinder;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 自动绑定配置属性，同一个类/方法可注解多次，从而实现绑定不同的前缀配置
 *
 * @author kfyty725
 * @date 2022/5/25 22:36
 * @email kfyty725@hotmail.com
 * @see DataBinder
 * @see com.kfyty.loveqq.framework.boot.processor.ConfigurationPropertiesBeanPostProcessor
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(ConfigurationProperties.ConfigurationPropertiesContainer.class)
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

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @interface ConfigurationPropertiesContainer {
        ConfigurationProperties[] value();
    }
}
