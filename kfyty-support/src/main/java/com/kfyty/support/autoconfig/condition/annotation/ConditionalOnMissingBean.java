package com.kfyty.support.autoconfig.condition.annotation;

import com.kfyty.support.autoconfig.condition.OnMissingBeanCondition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: bean 不存在条件
 *
 * @author kfyty725
 * @date 2022/4/17 11:40
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnMissingBeanCondition.class)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ConditionalOnMissingBean {
    /**
     * 需要 BeanFactory 中不存在的 bean 类型
     * 默认为被该注解所声明的类型
     *
     * @return bean type
     */
    Class<?>[] value() default {};

    /**
     * 需要 BeanFactory 中不存在的 bean name
     *
     * @return bean name
     */
    String[] name() default {};
}
