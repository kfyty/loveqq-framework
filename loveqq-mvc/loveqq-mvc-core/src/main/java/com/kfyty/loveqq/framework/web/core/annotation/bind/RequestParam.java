package com.kfyty.loveqq.framework.web.core.annotation.bind;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当 value 为空时，则需要 -parameters 编译参数支持
 * 该注解绑定逻辑和 {@link ConfigurationProperties} 注解绑定逻辑相同
 * 因此对应的参数传参方式也相同
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {

    String value() default "";

    String defaultValue() default "";
}
