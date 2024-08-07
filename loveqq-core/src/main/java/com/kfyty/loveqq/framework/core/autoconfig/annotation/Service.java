package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import com.kfyty.loveqq.framework.core.lang.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 标记该类为一个 bean 定义
 *
 * @author kfyty725
 * @date 2021/6/12 11:28
 * @email kfyty725@hotmail.com
 */
@Component
@Documented
@AspectResolve
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
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
