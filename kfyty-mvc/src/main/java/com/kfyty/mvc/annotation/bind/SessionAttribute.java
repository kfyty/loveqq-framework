package com.kfyty.mvc.annotation.bind;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 从 {@link javax.servlet.http.HttpSession} 中取值
 *
 * @see com.kfyty.mvc.request.resolver.SessionAttributeMethodArgumentResolver
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SessionAttribute {
    String value() default "";
}
