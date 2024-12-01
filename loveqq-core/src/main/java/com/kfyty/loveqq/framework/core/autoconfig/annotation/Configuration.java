package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.meta.This;
import com.kfyty.loveqq.framework.core.lang.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 标记该类为一个 bean 配置
 *
 * @author kfyty725
 * @date 2021/6/12 11:28
 * @email kfyty725@hotmail.com
 * @see com.kfyty.loveqq.framework.boot.instrument.ConfigurationClassInstrument
 */
@This
@Component
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {
    /**
     * bean name
     *
     * @return bean name
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * 是否进行了字节码级别的增强
     *
     * @return 默认 false
     */
    @AliasFor(annotation = This.class)
    boolean instrument() default true;
}
