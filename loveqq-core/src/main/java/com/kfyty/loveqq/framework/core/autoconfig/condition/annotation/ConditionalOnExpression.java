package com.kfyty.loveqq.framework.core.autoconfig.condition.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.condition.OnExpressionCondition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 表达式条件
 *
 * @author kfyty725
 * @date 2022/4/17 11:37
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnExpressionCondition.class)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ConditionalOnExpression {
    /**
     * 基于 ognl 的表达式
     * root 上下文包括配置文件属性，以及 {@code beanFactory}
     *
     * @return ognl 表达式
     * @see com.kfyty.loveqq.framework.core.utils.OgnlUtil#getBoolean(String, Object)
     */
    String value() default "";
}
