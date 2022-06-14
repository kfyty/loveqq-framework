package com.kfyty.support.autoconfig.condition.annotation;

import com.kfyty.support.autoconfig.condition.OnSingleCandidateCondition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 仅存在单个 bean 条件
 *
 * @author kfyty725
 * @date 2022/5/23 16:59
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnSingleCandidateCondition.class)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ConditionalOnSingleCandidate {
    /**
     * 限定 BeanFactory 中仅存在单个 bean 的 bean 类型
     * 默认为被该注解所声明的类型
     *
     * @return bean type
     */
    Class<?>[] value() default {};

    /**
     * 限定 BeanFactory 中仅存在单个 bean 的 bean name
     *
     * @return bean name
     */
    String[] name() default {};
}
