package com.kfyty.loveqq.framework.web.core.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.lang.annotation.AliasFor;
import com.kfyty.loveqq.framework.web.core.handler.AnnotatedExceptionHandler;
import com.kfyty.loveqq.framework.web.core.handler.ControllerAdviceExceptionHandlerRegistry;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 全局异常处理
 * 如果自定义处理，可以返回 null，获取线程上下文的请求响应对应自行处理
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 * @see Controller
 * @see ControllerAdviceExceptionHandlerRegistry
 * @see AnnotatedExceptionHandler
 */
@Component
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerAdvice {
    /**
     * 指定 bean name
     *
     * @return bean name
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * 是否忽略配置的扫描范围，而直接支持全部的异常处理
     * 默认 false，返回 true 时可同时支持网关异常的处理
     *
     * @return true/false
     */
    boolean supportAny() default false;

    /**
     * 基础包名
     *
     * @return 基础包名
     */
    String[] basePackages() default {};

    /**
     * 基础 class
     *
     * @return 基础 class
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * 指定的注解
     *
     * @return 默认 {@link Controller}
     */
    Class<? extends Annotation>[] annotations() default Controller.class;

    /**
     * 设置响应的 content-type
     *
     * @return content-type
     */
    String produces() default RequestMapping.DEFAULT_PRODUCES;
}
