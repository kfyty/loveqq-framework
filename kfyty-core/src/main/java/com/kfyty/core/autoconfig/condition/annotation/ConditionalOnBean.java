package com.kfyty.core.autoconfig.condition.annotation;

import com.kfyty.core.autoconfig.condition.OnBeanCondition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: bean 存在条件
 *
 * @author kfyty725
 * @date 2022/4/17 11:37
 * @email kfyty725@hotmail.com
 */
@Documented
@Conditional(OnBeanCondition.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ConditionalOnBean {
    /**
     * 需要 BeanFactory 中存在的 bean 类型
     * 默认为被该注解所声明的类型
     *
     * @return bean type
     */
    Class<?>[] value() default {};

    /**
     * 需要 BeanFactory 中存在的 bean name
     *
     * @return bean name
     */
    String[] name() default {};
}
