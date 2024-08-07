package com.kfyty.loveqq.framework.web.core.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.AspectResolve;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.lang.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识为一个控制器
 */
@Component
@Documented
@AspectResolve
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
    /**
     * bean name
     *
     * @return bean name
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * @see AspectResolve#value()
     */
    @AliasFor(value = "value", annotation = AspectResolve.class)
    boolean resolve() default true;
}
