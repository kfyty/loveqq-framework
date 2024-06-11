package com.kfyty.loveqq.framework.core.autoconfig.condition.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.condition.OnMissingClassCondition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 类路径 class 不存在条件
 *
 * @author kfyty725
 * @date 2022/4/17 11:42
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnMissingClassCondition.class)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ConditionalOnMissingClass {
    /**
     * 类路径上不应该存在的 class
     *
     * @return class 的全限定名
     */
    String[] value() default {};
}
