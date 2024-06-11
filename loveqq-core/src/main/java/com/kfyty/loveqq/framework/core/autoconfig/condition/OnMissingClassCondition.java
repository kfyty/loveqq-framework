package com.kfyty.loveqq.framework.core.autoconfig.condition;

import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingClass;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/4/17 11:43
 * @email kfyty725@hotmail.com
 */
public class OnMissingClassCondition extends OnClassCondition {

    @Override
    public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
        return !super.isMatch(context, metadata);
    }

    @Override
    protected String[] conditionNames(AnnotationMetadata<?> metadata) {
        return ((ConditionalOnMissingClass) metadata.get()).value();
    }
}
