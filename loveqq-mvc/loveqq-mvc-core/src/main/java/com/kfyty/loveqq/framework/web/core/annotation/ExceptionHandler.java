package com.kfyty.loveqq.framework.web.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 异常处理
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 * @see ControllerAdvice
 * @see com.kfyty.loveqq.framework.web.core.handler.AnnotatedExceptionHandler
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionHandler {
    /**
     * 可以处理的异常类型
     *
     * @return 异常类型
     */
    Class<? extends Throwable>[] value() default {};
}
