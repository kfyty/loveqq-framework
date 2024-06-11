package com.kfyty.loveqq.framework.boot.validator.annotation;

import com.kfyty.loveqq.framework.boot.validator.constraints.ConditionConstraintValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 条件校验器
 *
 * @author kfyty725
 * @date 2023/4/14 14:21
 * @email kfyty725@hotmail.com
 */
@Documented
@Repeatable(Condition.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Constraint(validatedBy = ConditionConstraintValidator.class)
public @interface Condition {
    /**
     * 条件表达式，根对象为当前属性所在对象
     * 基于 ognl 计算，返回值必须为 boolean 类型
     * 不为空时，忽略 {@link this#when()}、{@link this#then()}
     *
     * @return 条件表达式
     */
    String value() default "";

    /**
     * 条件判断，成立时判断 {@link this#then()}
     *
     * @return if express
     */
    String when() default "";

    /**
     * 条件断言，返回值为 true 时表示条件成立
     *
     * @return then express
     */
    String then() default "";

    /**
     * 属性值为 null 时是否继续校验条件
     * 返回 false 时将校验通过
     *
     * @return true/false
     */
    boolean continueIfNull() default true;

    String message() default "{javax.validation.constraints.Condition.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
    @interface List {
        Condition[] value();
    }
}
