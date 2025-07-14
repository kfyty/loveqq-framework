package com.kfyty.loveqq.framework.web.core.annotation.bind;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;

/**
 * 从 http session 中取值
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 * @see com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver.SessionAttributeMethodArgumentResolver
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SessionAttribute {
    /**
     * 参数名称，默认取 {@link Parameter#getName()}，需要 -parameters 编译参数支持
     *
     * @return 参数名称
     */
    String value() default "";

    /**
     * 参数值是否必须存在
     *
     * @return true/false
     */
    boolean required() default true;

    /**
     * 默认值
     *
     * @return 默认值
     */
    String defaultValue() default "";
}
