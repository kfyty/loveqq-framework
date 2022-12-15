package com.kfyty.mvc.annotation.bind;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当 value 为空时，则需要 -parameters 编译参数支持
 * 该注解绑定逻辑和 {@link com.kfyty.core.autoconfig.annotation.ConfigurationProperties} 注解绑定逻辑相同
 * 因此对应的参数传参方式也相同
 *
 * @see com.kfyty.mvc.request.resolver.RequestParamMethodArgumentResolver
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {

    String value() default "";

    String defaultValue() default "";
}
