package com.kfyty.loveqq.framework.web.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 异常处理
 *
 * @see com.kfyty.loveqq.framework.web.core.handler.AnnotatedExceptionHandler
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionHandler {
    Class<? extends Throwable>[] value() default {};
}
