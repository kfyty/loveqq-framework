package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.meta.This;
import com.kfyty.loveqq.framework.core.autoconfig.delegate.Delegate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 委托注解，必须同时注解在类上时才有效
 *
 * @author kfyty725
 * @date 2021/6/26 11:03
 * @email kfyty725@hotmail.com
 * @see Delegate
 */
@This
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface By {
    /**
     * 被委托的 bean name，优先级高于 {@link #by()}
     *
     * @return bean name
     */
    String byName() default "";

    /**
     * 被委托的类，默认是当前类的父类，此时使用当前类的实例执行，
     * 否则认为是容器内的 bean type，此时使用 {@link com.kfyty.loveqq.framework.core.utils.IOC#getBean(Class)} 作为实例执行
     *
     * @return 被委托的 class
     */
    Class<?> by() default Object.class;

    /**
     * 被委托的方法名称，默认是当前方法名称
     *
     * @return 被委托的方法
     */
    String method() default "";
}
