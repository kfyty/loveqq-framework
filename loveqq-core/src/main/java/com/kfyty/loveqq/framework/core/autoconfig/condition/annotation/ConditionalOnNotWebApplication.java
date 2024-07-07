package com.kfyty.loveqq.framework.core.autoconfig.condition.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.condition.OnNotWebApplicationCondition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 非 web 应用条件
 *
 * @author kfyty725
 * @date 2022/8/28 17:20
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(OnNotWebApplicationCondition.class)
public @interface ConditionalOnNotWebApplication {
    /**
     * web 应用类型
     *
     * @return 应用类型
     */
    ConditionalOnWebApplication.WebApplicationType value() default ConditionalOnWebApplication.WebApplicationType.NONE;
}
