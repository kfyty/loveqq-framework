package com.kfyty.core.autoconfig.condition;

import com.kfyty.core.support.AnnotationMetadata;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/8/28 17:21
 * @email kfyty725@hotmail.com
 */
public class OnNotWebApplicationCondition extends OnWebApplicationCondition {

    @Override
    public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
        return !super.isMatch(context, metadata);
    }
}
