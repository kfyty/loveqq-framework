package com.kfyty.boot.validator.annotation;

import com.kfyty.boot.validator.constraints.FlexibleConstraintValidator;
import com.kfyty.boot.validator.constraints.FlexibleValidator;
import com.kfyty.boot.validator.context.IOCContext;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

/**
 * 描述: 自定义校验
 *
 * @author kfyty725
 * @date 2022/10/17 9:57
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = FlexibleConstraintValidator.class)
public @interface Flexible {
    /**
     * 校验逻辑实现类
     */
    Class<? extends FlexibleValidator<?>> value();

    /**
     * 是否支持 ioc 容器
     * 返回 true 时需设置 {@link IOCContext#setIOC(Function)}
     *
     * @see com.kfyty.boot.validator.context.IOCContext
     */
    boolean ioc() default true;

    String message() default "{javax.validation.constraints.Flexible.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
