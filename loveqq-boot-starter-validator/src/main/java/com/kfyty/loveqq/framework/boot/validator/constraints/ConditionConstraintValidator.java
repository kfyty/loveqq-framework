package com.kfyty.loveqq.framework.boot.validator.constraints;

import com.kfyty.loveqq.framework.boot.validator.annotation.Condition;
import com.kfyty.loveqq.framework.boot.validator.context.ValidatorContext;
import com.kfyty.loveqq.framework.core.utils.OgnlUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.notEmpty;

/**
 * 描述: 条件校验器
 *
 * @author kfyty725
 * @date 2023/4/14 14:55
 * @email kfyty725@hotmail.com
 */
public class ConditionConstraintValidator implements ConstraintValidator<Condition, Object> {
    /**
     * 条件注解
     */
    private Condition condition;

    @Override
    public void initialize(Condition constraintAnnotation) {
        this.condition = constraintAnnotation;
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return this.isMatch(value, condition, context);
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
        Object currentBean = ValidatorContext.getValueContext().getCurrentBean();
        if (notEmpty(condition.value())) {
            return OgnlUtil.getBoolean(condition.value(), currentBean);
        }
        if (OgnlUtil.getBoolean(condition.when(), currentBean)) {
            return OgnlUtil.getBoolean(condition.then(), currentBean);
        }
        return true;
    }
}
