package com.kfyty.support.autoconfig.condition;

import com.kfyty.support.autoconfig.condition.annotation.ConditionalOnClass;
import com.kfyty.support.wrapper.AnnotationWrapper;

import static com.kfyty.support.utils.ReflectUtil.isPresent;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/4/17 11:43
 * @email kfyty725@hotmail.com
 */
public class OnClassCondition implements Condition {

    @Override
    public boolean isMatch(ConditionContext context, AnnotationWrapper<?> metadata) {
        for (String conditionName : this.conditionNames(metadata)) {
            if (!isPresent(conditionName)) {
                return false;
            }
        }
        return true;
    }

    protected String[] conditionNames(AnnotationWrapper<?> metadata) {
        return ((ConditionalOnClass) metadata.get()).value();
    }
}
