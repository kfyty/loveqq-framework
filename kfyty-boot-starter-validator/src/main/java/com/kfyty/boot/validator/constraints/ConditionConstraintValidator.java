package com.kfyty.boot.validator.constraints;

import com.kfyty.boot.validator.annotation.Condition;
import com.kfyty.boot.validator.context.ValidatorContext;
import com.kfyty.boot.validator.exception.ConditionConstraintException;
import com.kfyty.core.utils.OgnlUtil;
import com.kfyty.core.utils.ReflectUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.valuecontext.ValueContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import static com.kfyty.core.utils.AnnotationUtil.findAnnotation;
import static com.kfyty.core.utils.AnnotationUtil.flatRepeatableAnnotation;
import static com.kfyty.core.utils.CommonUtil.notEmpty;

/**
 * 描述: 条件校验器
 *
 * @author kfyty725
 * @date 2023/4/14 14:55
 * @email kfyty725@hotmail.com
 */
public class ConditionConstraintValidator implements ConstraintValidator<Condition, Object> {
    /**
     * {@link ConstraintValidatorContextImpl#basePath} 属性缓存
     */
    private static final Field PATH_FIELD = ReflectUtil.getField(ConstraintValidatorContextImpl.class, "basePath");

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            Condition[] conditions = this.getConditions(context);
            for (Condition condition : conditions) {
                if (!this.isMatch(value, condition, context)) {
                    return false;
                }
            }
            return true;
        } catch (ConditionConstraintException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
            return false;
        }
    }

    /**
     * 校验条件是否满足
     *
     * @param value     校验值
     * @param condition 条件
     * @param context   context
     * @return true id match
     */
    public boolean isMatch(Object value, Condition condition, ConstraintValidatorContext context) {
        if (value == null && !condition.continueIfNull()) {
            return true;
        }
        try {
            Object currentBean = ValidatorContext.getValueContext().getCurrentBean();
            if (notEmpty(condition.value())) {
                if (!OgnlUtil.getBoolean(condition.value(), currentBean)) {
                    throw new ConditionConstraintException(notEmpty(condition.message()) ? condition.message() : "express does not match: " + condition.value());
                }
                return true;
            }
            if (OgnlUtil.getBoolean(condition.when(), currentBean)) {
                if (!OgnlUtil.getBoolean(condition.then(), currentBean)) {
                    throw new ConditionConstraintException(notEmpty(condition.message()) ? condition.message() : "express does not match: " + condition.then());
                }
            }
            return true;
        } catch (ConditionConstraintException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ConditionConstraintException(e.getCause() == null ? e.getMessage() : e.getCause().getMessage(), e.getCause() == null ? e : e.getCause());
        }
    }

    /**
     * 获取条件注解
     *
     * @return 条件注解
     */
    public Condition[] getConditions(ConstraintValidatorContext context) {
        ValueContext<?, ?> valueContext = ValidatorContext.getValueContext();
        if (valueContext == null) {
            return new Condition[0];
        }
        PathImpl path = (PathImpl) ReflectUtil.getFieldValue(context, context instanceof ConstraintValidatorContextImpl ? PATH_FIELD : ReflectUtil.getField(context.getClass(), "basePath"));
        Field field = ReflectUtil.getField(valueContext.getCurrentBean().getClass(), path.getLeafNode().getName());
        Condition condition = findAnnotation(field, Condition.class);
        if (condition != null) {
            return new Condition[]{condition};
        }
        return (Condition[]) flatRepeatableAnnotation(new Annotation[]{findAnnotation(field, Condition.List.class)}, new Condition[0]);
    }
}
