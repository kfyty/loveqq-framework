package com.kfyty.loveqq.framework.web.core.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.lang.annotation.AliasFor;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 全局异常处理
 * 返回值的处理方式将与所访问的控制器方法的返回值处理方式保持一致
 * 如果自定义处理，可以返回 null，获取线程上下文的请求响应对应自行处理
 *
 * @see Controller
 * @see com.kfyty.loveqq.framework.web.core.handler.ControllerAdviceExceptionHandlerResolver
 * @see com.kfyty.loveqq.framework.web.core.handler.AnnotatedExceptionHandler
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
}
