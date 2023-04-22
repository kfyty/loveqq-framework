package com.kfyty.boot.validator.constraints;

import com.kfyty.boot.validator.annotation.Flexible;
import com.kfyty.boot.validator.context.IOCContext;
import com.kfyty.core.utils.ReflectUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

/**
 * 描述: 自定义校验器
 *
 * @author kfyty725
 * @date 2022/10/17 10:02
 * @email kfyty725@hotmail.com
 */
public class FlexibleConstraintValidator implements ConstraintValidator<Flexible, Object> {
    private FlexibleValidator<?> validator;

    @Override
    public void initialize(Flexible constraintAnnotation) {
        Class<? extends FlexibleValidator<?>> value = constraintAnnotation.value();
        if (!constraintAnnotation.ioc()) {
            this.validator = ReflectUtil.newInstance(value);
            return;
        }
        this.validator = Objects.requireNonNull(IOCContext.getBean(value), "bean does not exists: " + value);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            return this.validator.valid(value);
        } catch (Exception e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
            return false;
        }
    }
}
