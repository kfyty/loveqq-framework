package com.kfyty.core.autoconfig.condition.annotation;

import com.kfyty.core.autoconfig.condition.Condition;
import com.kfyty.core.autoconfig.condition.ConditionContext;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 条件注解的元注解
 * 条件匹配时，存在 {@link Conditional} 注解元素的 clazz 会延迟生成 bean 定义
 * 并且条件匹配时，只能依赖当时已经存在的 bean 定义，因此条件 bean 的顺序可能需要关注
 * 当存在循环条件时，默认策略为匹配失败
 *
 * @author kfyty725
 * @date 2022/4/17 11:27
 * @email kfyty725@hotmail.com
 * @see ConditionContext
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Conditional {
    /**
     * 要匹配的全部条件
     *
     * @return condition
     */
    Class<? extends Condition>[] value();
}
