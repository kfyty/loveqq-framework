package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 表示是否解析 bean 是否具有可切入的切面
 *
 * @author kfyty725
 * @date 2024/8/7 18:35
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AspectResolve {
    /**
     * 是否要解析切面
     *
     * @return 默认 true
     * @see com.kfyty.loveqq.framework.aop.processor.AspectJBeanPostProcessor
     */
    boolean value() default true;
}
