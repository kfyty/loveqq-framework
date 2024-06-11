package com.kfyty.loveqq.framework.web.core.annotation;

import com.kfyty.loveqq.framework.web.core.proxy.ControllerExceptionAdviceInterceptorProxy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 异常处理
 *
 * @see ControllerExceptionAdviceInterceptorProxy
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionHandler {
    Class<? extends Throwable>[] value() default {};
}
