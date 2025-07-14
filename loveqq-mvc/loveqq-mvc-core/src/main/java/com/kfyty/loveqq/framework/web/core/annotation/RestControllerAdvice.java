package com.kfyty.loveqq.framework.web.core.annotation;

import com.kfyty.loveqq.framework.core.lang.annotation.AliasFor;
import com.kfyty.loveqq.framework.web.core.annotation.bind.ResponseBody;
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
 * @see RestController
 * @see ControllerAdviceExceptionHandlerRegistry
 * @see AnnotatedExceptionHandler
 */
@Documented
@ResponseBody
@ControllerAdvice
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestControllerAdvice {
    /**
     * 指定 bean name
     *
     * @return bean name
     */
    @AliasFor(annotation = ControllerAdvice.class)
    String value() default "";

    /**
     * 基础包名
     *
     * @return 基础包名
     */
    @AliasFor(annotation = ControllerAdvice.class)
    String[] basePackages() default {};

    /**
     * 基础 class
     *
     * @return 基础 class
     */
    @AliasFor(annotation = ControllerAdvice.class)
    Class<?>[] basePackageClasses() default {};

    /**
     * 指定的注解
     *
     * @return 默认 {@link Controller}
     */
    @AliasFor(annotation = ControllerAdvice.class)
    Class<? extends Annotation>[] annotations() default Controller.class;

    /**
     * 设置响应的 content-type
     *
     * @return content-type
     */
    @AliasFor(annotation = ControllerAdvice.class)
    String produces() default RequestMapping.DEFAULT_PRODUCES;
}
